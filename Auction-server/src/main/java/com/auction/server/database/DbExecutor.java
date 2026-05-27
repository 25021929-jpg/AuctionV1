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

            // Truyền session vào action để Repository dùng getCurrentSession()
            // Service không gọi action(session) — action là lambda không tham số
            // Session được lấy ngầm qua getCurrentSession() trong Repository
            action.run(); // Lambda không tham số! Service hoàn toàn mù tịt về Session.
            session.getTransaction().commit();

        } catch (Exception e) {
            // Rollback trước khi throw — không để transaction treo
            if (session.getTransaction() != null &&
                    session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        }
        // Không đóng session ở đây!
        // Với thread context, Hibernate tự quản lý vòng đời session
    }

    /**
     * Chạy logic có transaction — trả về giá trị.
     * Dùng cho: placeBid trả về BidResult, createAuction trả về AuctionSession...
     *
     * @param action lambda trả về kết quả
     * @return kết quả từ lambda
     */
    // Dùng cho logic có trả về kết quả
    public static <T> T runAndReturn(Supplier<T> action) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            T result = action.get();
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (session.getTransaction() != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Chạy logic chỉ đọc (Read-only).
     * Dùng cho: getAuctionList, getUserProfile, getBidHistory...
     */
    public static <T> T query(Supplier<T> action) {
        Session session = sessionFactory.getCurrentSession();
        try {
            // Báo cho Hibernate: "Đừng chụp Snapshot (Dirty Checking)"
            session.setDefaultReadOnly(true);

            // Vẫn phải mở Transaction để Hibernate quản lý vòng đời Session
            session.beginTransaction();

            T result = action.get();

            // Commit để báo DB kết thúc và Hibernate tự động ĐÓNG Session, dọn RAM
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (session.getTransaction() != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        } finally {
            // Trả lại trạng thái (thực ra sau commit session đã đóng,
            // nhưng viết cẩn thận nếu dùng cơ chế open-session-in-view)
            if (session.isOpen()) {
                session.setDefaultReadOnly(false);
            }
        }
    }
}