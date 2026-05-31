Dưới đây là nội dung lý thuyết được chuẩn hóa theo định dạng Markdown (`.md`). Bạn có thể sao chép trực tiếp đoạn này để lưu vào file tài liệu (ví dụ: `README.md` hoặc `ARCHITECTURE.md`) trong dự án đấu giá của mình làm tài liệu tham khảo khoa học.

---

# Kiến Trúc Hệ Thống: Quản Lý Luồng và Thiết Kế Controller Trong Dự Án Đấu Giá

Tài liệu này giải thích chi tiết về mặt khoa học máy tính lý do tại sao hệ thống sử dụng các **Stateless Object (Đối tượng không trạng thái)** được quản lý bởi **Dependency Injection (DI)** làm Controller thay vì sử dụng các **Static Utility Class (Lớp tiện ích Static)**, dù chúng có hành vi thực thi tương đồng khi xử lý đa luồng.

---

## 1. Nguyên Lý Quản Lý Bộ Nhớ và Xử Lý Đa Luồng (Multi-threading)

Trong môi trường thực thi của Java (JVM), kiến trúc bộ nhớ được phân chia nghiêm ngặt giữa các luồng:

* **Vùng nhớ Heap (Chia sẻ toàn cục):** Tất cả các Đối tượng (Objects) được khởi tạo đều nằm trên Heap. Các luồng (Threads) trong cùng một tiến trình có đặc quyền truy cập vào Heap như nhau. Nếu một đối tượng duy nhất (Singleton) trên Heap có chứa các thuộc tính thay đổi (`instance fields`), việc nhiều luồng cùng gọi các phương thức chỉnh sửa thuộc tính đó mà không có cơ chế đồng bộ (`Lock`/`Synchronization`) sẽ dẫn đến **Race Condition** và bất đồng bộ dữ liệu.
* **Vùng nhớ Stack (Cục bộ theo luồng):** Mỗi luồng khi được tạo ra đều có một vùng nhớ Stack riêng biệt và độc lập. Khi một luồng gọi một phương thức, các tham số đầu vào và biến cục bộ (local variables) định nghĩa bên trong phương thức đó sẽ được lưu trữ hoàn toàn trên Stack của luồng đó.

### Tính chất "Stateless" của Controller trong dự án:

Các Controller trong dự án đấu giá được thiết kế theo mô hình **Stateless (Không trạng thái)**. Các phương thức xử lý (như đặt giá, xem thông tin đấu giá) không thay đổi hoặc không dựa vào bất kỳ thuộc tính nội tại (fields) nào của chính Controller đó.

Do đó, khi hàng trăm Client gửi request đồng thời, hệ thống sẽ sinh ra hàng trăm luồng cùng truy cập vào một phương thức của một Object Controller duy nhất. Tuy nhiên, mỗi luồng sẽ tự xử lý trên vùng nhớ Stack riêng của nó, đảm bảo tính **Thread-safe** tuyệt đối mà không cần dùng cơ chế khóa (giúp tối ưu hóa hiệu năng, tránh nghẽn luồng).

---

## 2. So Sánh Giữa Sử Dụng DI Object (Stateless Bean) và Static Utility Class

Về mặt **hành vi thực thi (Execution)**, một phương thức của Stateless Object và một hàm `static` hoàn toàn giống nhau: Nhận tham số $\rightarrow$ Xử lý trên Stack $\rightarrow$ Trả kết quả. Tuy nhiên, dự án lựa chọn **DI Object** vì các lý do kiến trúc phần mềm cốt lõi sau:

### 2.1. Khả năng Kiểm thử Độc lập (Unit Testing & Mocking)

* **Hạn chế của Static:** Các lời gọi hàm static (ví dụ: `AuctionUtil.processBid()`) bị liên kết cứng (Hard-coded) vào mã nguồn. Khi viết Unit Test cho tầng Controller, rất khó để giả lập (Mock) hành vi của các tầng bên dưới, dẫn đến việc bài test bị phụ thuộc vào dữ liệu thật hoặc môi trường thật.
* **Lợi ích của DI Object:** Bằng cách sử dụng Dependency Injection, Controller nhận các dịch vụ phụ thuộc thông qua Interface trong Constructor. Khi chạy thực tế, DI Container sẽ bơm đối tượng thật vào. Khi chạy Unit Test, lập trình viên có thể dễ dàng bơm một **Mock Object** vào để kiểm thử độc lập logic của Controller một cách nhanh chóng.

### 2.2. Nguyên lý Đảo ngược Phụ thuộc (Dependency Inversion Principle - SOLID)

* Hàm static không thể triển khai (implement) một `Interface` và không có tính đa hình. Việc gọi hàm static bắt buộc hệ thống phải phụ thuộc vào một Class cụ thể (Concrete Class).
* Với DI Object, Controller chỉ giao tiếp với các tầng dưới thông qua sự trừu tượng (`Interface`). Điều này cho phép hệ thống linh hoạt thay đổi logic bên dưới (ví dụ: đổi từ `MySqlRepository` sang `RedisCacheRepository` để tăng tốc độ xử lý đấu giá) thông qua cấu hình DI mà không cần chỉnh sửa bất kỳ dòng code nào trong Controller.

### 2.3. Quản lý Vòng đời (Lifecycle) và Cấu hình Động

* Các Object được quản lý bởi DI Container có vòng đời rõ ràng (Khởi tạo, Tiêm phụ thuộc, Hủy bỏ). Container có thể dễ dàng inject các cấu hình động từ môi trường bên ngoài (file `.properties` hoặc `.env` như thời gian đấu giá, bước giá tối thiểu) vào thuộc tính *Read-only* của Object ngay khi khởi tạo.
* Ngược lại, việc nạp cấu hình động vào các biến `static` rất phức tạp, dễ gây ra lỗi khởi tạo muộn (`NullPointerException`) do thứ tự nạp Class của JVM.

### 2.4. Khả năng Mở rộng bằng Proxy và AOP (Aspect-Oriented Programming)

* Do các hàm static thuộc về Class chứ không thuộc về Object, các Framework không thể can thiệp vào giữa quá trình thực thi của hàm.
* Với DI Object, hệ thống có thể áp dụng các cơ chế bọc dữ liệu như **Dynamic Proxy** hoặc **AOP**. Điều này cho phép tự động chèn các logic xuyên suốt hệ thống như: Ghi log thời gian phản hồi của lượt đấu giá, tự động quản lý đóng/mở Transaction (`@Transactional`), hoặc kiểm tra quyền hạn (Security) ngay trước khi hàm của Controller được thực thi.

---

**Kết luận:** Việc lựa chọn Object không trạng thái kết hợp với cơ chế Dependency Injection là giải pháp tối ưu, vừa tận dụng được hiệu suất tối đa của bộ nhớ Stack trong xử lý đa luồng (giống hàm static), vừa đảm bảo mã nguồn tuân thủ nghiêm ngặt các nguyên lý thiết kế hướng đối tượng (OOP) sạch, dễ bảo trì và dễ mở rộng.