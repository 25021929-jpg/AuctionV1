package com.auction.server.database;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.function.Supplier;

/**
 * DbExecutor: lớp tiện ích quản lý Transaction cho Service.
 *
 * Tại sao cần class này?
 * Service cần quản lý transaction nhưng không được biết về Session.
 * DbExecutor đóng vai trò "gác cổng":
 *   - Mở session + transaction trước khi chạy logic
 *   - Commit khi thành công
 *   - Rollback khi có exception
 *   - Đóng session trả về pool
 * Service chỉ truyền lambda chứa logic nghiệp vụ vào.
 *
 * Tương đương với @Transactional của Spring Boot —
 * nhưng bạn tự viết nên hiểu từng bánh răng.
 */
public class DbExecutor {

    private static SessionFactory sessionFactory;

    public static void init(SessionFactory sf) {
        sessionFactory = sf;
    }

    /**
     * Chạy logic có transaction — không trả về giá trị.
     * Dùng cho: save, update, delete, đặt giá...
     *
     * @param action lambda chứa logic Service cần chạy trong transaction
     */
    // Dùng cho logic không trả về kết quả (void)
    public static void run(Runnable action) {
        // getCurrentSession(): lấy session của Thread hiện tại
        // Nếu chưa có → tạo mới và bind vào Thread
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            // Không bật readOnly ở run(): đây là nhánh dành cho mutation (save/update/delete).
            // Nếu setDefaultReadOnly(true), Hibernate có thể bỏ qua dirty checking và làm thao tác ghi không có hiệu lực.

            // Truyền session vào action để Repository dùng getCurrentSession()
            // Service không gọi action(session) — action là lambda không tham số
            // Session được lấy ngầm qua getCurrentSession() trong Repository
            action.run(); // Lambda không tham số! Service hoàn toàn mù tịt về Session.
            session.getTransaction().commit();
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    public static <T> T runAndReturn(Supplier<T> action) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            T result = action.get();
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            handleException(session, e);
            return null; // unreachable
        }
    }

    public static <T> T query(Supplier<T> action) {
        Session session = sessionFactory.getCurrentSession();
        try {


            // Vẫn phải mở Transaction để Hibernate quản lý vòng đời Session
            session.beginTransaction();
            // Báo cho Hibernate: "Đừng chụp Snapshot (Dirty Checking)"
            session.setDefaultReadOnly(true);

            T result = action.get();

            // Commit để báo DB kết thúc và Hibernate tự động ĐÓNG Session, dọn RAM
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            handleException(session, e);
            return null; // unreachable
        } finally {
            if (session.isOpen()) {
                session.setDefaultReadOnly(false);
            }
        }
    }

    private static void handleException(Session session, Exception e) {
        if (session.getTransaction() != null && session.getTransaction().isActive()) {
            session.getTransaction().rollback();
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new RuntimeException("Database error", e);
    }
}
