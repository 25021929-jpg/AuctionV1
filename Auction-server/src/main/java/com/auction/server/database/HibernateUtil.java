package com.auction.server.database;

import com.auction.server.entity.*;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * HibernateUtil(tên gọi truyền thống) nhưng thực tế nên được gọi là SessionFactoryProvider vì nhiệm
 * vụ của nó: + Nói rõ đây là provider — cung cấp session + Thể hiện đây là infrastructure quan
 * trọng + Lifecycle rõ ràng: init → use → destroy // Tên nói lên đây là tầng persistence // Không
 * phải helper function đơn giản
 */
public class HibernateUtil {

  // Nơi lưu trữ "Nhà máy sản xuất Session" duy nhất cho toàn Server (Singleton)
  private static SessionFactory sessionFactory;

  /**
   * Khởi tạo cấu hình Hibernate bằng cách nhận một DataSource đã được tạo sẵn từ bên ngoài.
   * * @param dataSource Đối tượng quản lý kết nối (ví dụ: HikariCP) đang nằm sẵn trên Heap.
   */
  public static synchronized void initialize(HikariDataSource dataSource) {
    // Khóa bảo vệ (Guard Clause): Nếu đã khởi tạo rồi thì bỏ qua, tránh tạo đè gây rò rỉ (leak)
    // vùng nhớ
    if (sessionFactory != null) return;

    // Tạo đối tượng cấu hình trống của Hibernate
    Configuration config = new Configuration();

    /*
     * CÚ PHÁP VÀNG: Ném thẳng địa chỉ vùng nhớ (Reference) của Object dataSource vào cấu hình.
     * * - "hibernate.connection.datasource" là key chuẩn của Hibernate để nhận DataSource instance.
     * - Khác với phương thức setProperty() chỉ nhận chuỗi String (String-based),
     * việc dùng .put() vào Map Properties cho phép ta truyền một Object thật sự đang sống trên RAM.
     * - Khi nhận được Object này, Hibernate sẽ tự động kích hoạt bộ chuyển đổi nội bộ
     * (DatasourceConnectionProviderImpl) để mượn connection từ pool này.
     */
    config.getProperties().put("hibernate.connection.datasource", dataSource);

    // Định nghĩa "ngôn ngữ" giao tiếp với Database (ở đây là MySQL)
    config.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

    // Bật debug: In toàn bộ câu lệnh SQL ra Console để lập trình viên dễ theo dõi
    config.setProperty("hibernate.show_sql", "true");
    // Định dạng câu SQL cho đẹp mắt, dễ đọc thay vì viết trên một dòng thẳng tuột
    config.setProperty("hibernate.format_sql", "true");

    /*
     * Chế độ an toàn cho Production (validate):
     * Hibernate chỉ kiểm tra xem cấu hình các Class Entity Java có khớp 100% với các bảng dưới DB không.
     * Nếu lệch (thiếu cột, sai kiểu dữ liệu), nó sẽ báo lỗi và DỪNG SERVER NGAY LẬP TỨC.
     * Tuyệt đối không tự ý sửa đổi hay xóa dữ liệu của DB như các chế độ 'update' hay 'create-drop'.
     */
    config.setProperty("hibernate.hbm2ddl.auto", "validate");
    // Cấu hình hibernate bật threadBound
    config.setProperty(
        "hibernate.current_session_context_class", "thread" // mỗi thread có 1 session riêng
        );
    // ĐĂNG KÝ THÀNH VIÊN: Khai báo các Class Entity được quản lý bởi Hibernate ORM
    config.addAnnotatedClass(User.class);
    config.addAnnotatedClass(Category.class);
    config.addAnnotatedClass(AuctionItem.class);
    config.addAnnotatedClass(ItemImage.class);
    config.addAnnotatedClass(AuctionSession.class);
    config.addAnnotatedClass(Bid.class);
    config.addAnnotatedClass(Payment.class);
    config.addAnnotatedClass(WalletTransaction.class);

    // Kích hoạt cỗ máy: Đọc toàn bộ cấu hình trên để sinh ra SessionFactory.
    // Quá trình này nặng nhất, nên chỉ chạy duy nhất 1 lần lúc startup server.
    sessionFactory = config.buildSessionFactory();

    System.out.println("✅ Hibernate SessionFactory khởi động thành công!");
  }

  /**
   * Mở một phiên làm việc mới (Session) với Database. Mỗi Thread xử lý Request từ Client nên mở một
   * Session riêng và đóng lại ngay khi dùng xong.
   */
  public static Session openSession() {
    if (sessionFactory == null) {
      throw new IllegalStateException(
          "Cảnh báo: Bạn phải gọi HibernateUtil.initialize() trước khi mở Session!");
    }
    return sessionFactory.openSession();
  }

  /**
   * Đóng nhà máy SessionFactory khi Server tắt (Shutdown hook). Giải phóng toàn bộ bộ nhớ đệm
   * (Cache) và các tài nguyên nội bộ mà Hibernate nắm giữ.
   */
  public static void shutdown() {
    if (sessionFactory != null) {
      sessionFactory.close();
      System.out.println("💤 Hibernate SessionFactory đã đóng.");
    }
    /*
     * LƯU Ý KIẾN TRÚC: Ở đây ta KHÔNG gọi dataSource.close() nữa.
     * Vì dataSource được truyền từ bên ngoài vào (nhận qua tham số),
     * nên bên nào tạo ra dataSource thì bên đó phải có trách nhiệm đóng nó khi tắt server.
     * Điều này tuân thủ nguyên tắc: "Ai đẻ ra thì người đó nuôi và chôn cất".
     */
  }

  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }
}
