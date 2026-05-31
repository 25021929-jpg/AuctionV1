Dependency Injection (Tiêm phụ thuộc). Bằng cách đẩy HikariDataSource ra ngoài và truyền nó vào qua hàm initialize(dataSource), class HibernateUtil của bạn bây giờ đã đạt đến trạng thái Loosely Coupled (Liên kết lỏng lẻo) – tiêu chuẩn vàng của kiến trúc phần mềm.

Từ nay, HibernateUtil không còn quan tâm hay phụ thuộc vào việc cái DataSource kia được tạo ra bằng cách nào, mật khẩu là gì, hay dùng HikariCP hay thư viện khác. Nhiệm vụ duy nhất của nó là: Nhận ống nước và cấp cho Hibernate.

Việc khởi tạo DataSource do DatabaseConnection đảm nhiệm, còn HibernateUtil chỉ tập trung vào việc cấu hình và quản lý SessionFactory. Điều này giúp mã nguồn của bạn trở nên sạch sẽ, dễ bảo trì và dễ mở rộng trong tương lai. Nếu sau này bạn muốn đổi sang một thư viện connection pool khác hoặc thay đổi cách cấu hình DataSource, bạn chỉ cần chỉnh sửa trong DatabaseConnection mà không phải động chạm gì đến HibernateUtil.

-> Thỏa mãn các nguyên tắc SOLID, đặc biệt là Single Responsibility Principle (SRP) và Dependency Inversion Principle (DIP). Bạn đã tách biệt rõ ràng trách nhiệm của từng class và giảm sự phụ thuộc giữa chúng, tạo nên một hệ thống linh hoạt và dễ bảo trì.