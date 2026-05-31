Dưới đây là bản tổng hợp toàn bộ lý thuyết kiến trúc cốt lõi mà chúng ta đã bàn luận. Bạn có thể copy trực tiếp nội dung này vào file `.md` (Markdown) để làm tài liệu báo cáo hoặc đề cương ôn tập bảo vệ đồ án trước Hội đồng.

---

# TỔNG HỢP LÝ THUYẾT KIẾN TRÚC MÃ NGUỒN SERVER

*(Tài liệu chuẩn bị cho bảo vệ đồ án/phỏng vấn)*

## 1. Cơ chế Dependency Injection (DI) - Tiêm phụ thuộc

**Khái niệm:** DI là một kỹ thuật thiết kế phần mềm, trong đó một Object không tự tạo ra các Object mà nó phụ thuộc (dependencies). Thay vào đó, các dependencies này sẽ được khởi tạo từ bên ngoài và "tiêm" (inject) vào nó thông qua Constructor (hàm khởi tạo).

**Tại sao áp dụng?**

* **Giảm thiểu sự kết dính (Loose Coupling):** Các tầng không bị trói buộc cứng vào một công nghệ cụ thể (ví dụ: `BidService` không bị dính chặt vào `HibernateBidRepository`).
* **Tuân thủ DIP (Dependency Inversion Principle):** Các module cấp cao (Service) không phụ thuộc vào module cấp thấp (Repository) mà cả hai cùng phụ thuộc vào Interface (Tính trừu tượng).
* **Dễ dàng Unit Test:** Có thể dễ dàng tiêm các đối tượng giả (Mock Object) vào Controller/Service để kiểm thử logic nghiệp vụ mà không cần mở kết nối xuống Database thật.

## 2. Quản lý bộ nhớ Heap: Stateful vs. Stateless Objects

Hệ thống Server cần phục vụ hàng nghìn request, việc phân loại Object để khởi tạo là cực kỳ quan trọng để tránh tràn bộ nhớ (OutOfMemoryError).

* **Stateful Objects (Đối tượng mang trạng thái):**
* **Gồm:** `WireMessage`, `PlaceBidRequest`, `BidResponse`, v.v.
* **Bản chất:** Chứa dữ liệu của từng phiên giao dịch cụ thể.
* **Vòng đời:** Được sinh ra liên tục (dùng `new` hoặc Gson parse) theo mỗi request và bị Garbage Collector (GC) dọn dẹp ngay khi request kết thúc.


* **Stateless Objects (Đối tượng vô trạng thái):**
* **Gồm:** `BidController`, `BidService`, `HibernateBidRepository`.
* **Bản chất:** Chỉ chứa các hàm xử lý logic (công thức tính toán, câu lệnh SQL), hoàn toàn không lưu trữ dữ liệu cá nhân của user.
* **Vòng đời (Singleton Design Pattern):** Chỉ khởi tạo **DUY NHẤT 1 LẦN** lúc Server bật (tại `MainServer`). Hàng nghìn luồng (Thread) từ các Client khác nhau sẽ gọi chung vào một vùng nhớ chứa logic này, giúp tiết kiệm tối đa RAM.



## 3. Kiến trúc Phân tầng (Layered Architecture) & Chiều phụ thuộc

Dòng chảy dữ liệu và mũi tên phụ thuộc (Dependency Direction) trong hệ thống bắt buộc phải đi theo một chiều từ trên xuống dưới:

> **Client Socket** $\rightarrow$ **RequestDispatcher** $\rightarrow$ **Controller** $\rightarrow$ **Service** $\rightarrow$ **Repository** $\rightarrow$ **Database**

* **Nguyên tắc Vàng:** Tầng trên gọi và chứa (Inject) tầng dưới.
* **Sai lầm chí mạng (Anti-pattern):** Tuyệt đối không làm ngược lại (Ví dụ: Inject Service vào Repository). Điều này sẽ gây ra lỗi **Circular Dependency** (Phụ thuộc vòng tròn / Con gà quả trứng), khiến Server không thể khởi động do lỗi `StackOverflowError`.

## 4. Ba lý do "Tử thần" khi dùng từ khóa `new` ở tầng Controller/Service

Việc gõ `private final BidRepository repo = new HibernateBidRepository()` ngay trong class `BidService` vi phạm nghiêm trọng các tiêu chuẩn Production vì:

1. **Phá vỡ Tính đóng gói (Encapsulation):** Service đáng lẽ chỉ làm logic toán học, nay lại phải "biết" cả cách cấu hình kết nối DB (`HibernateUtil.getSessionFactory()`).
2. **Vi phạm Nguyên tắc Mở/Đóng (Open/Closed Principle):** Nếu dự án muốn đổi từ MySQL sang MongoDB, lập trình viên phải lội vào sửa code cốt lõi của tầng Service, nguy cơ cao làm vỡ logic cũ.
3. **Tê liệt hệ thống Test:** Không thể tách rời tầng DB để test riêng lẻ tầng logic. Mỗi lần chạy Test là một lần ép hệ thống kết nối DB thật, làm chậm và "làm bẩn" dữ liệu thực.

## 5. Composition Root (Xưởng lắp ráp trung tâm)

**Khái niệm:** Là vị trí duy nhất trong toàn bộ mã nguồn chịu trách nhiệm khởi tạo và đấu nối (wiring) toàn bộ các đối tượng logic của ứng dụng.

**Ứng dụng trong đồ án:**

* Nằm tại hàm `main` của file `MainServer.java`.
* Đóng vai trò như một **IoC Container thủ công** (Inversion of Control).
* **Quy trình lắp ráp (Từ gốc đến ngọn):**
1. Khởi tạo cấu hình (Database Pool, SessionFactory).
2. Khởi tạo tầng Đáy (Các Repositories).
3. Khởi tạo tầng Giữa (Các Services - Tiêm Repositories vào).
4. Khởi tạo tầng Giao tiếp (Các Controllers - Tiêm Services vào).
5. Khởi tạo tầng Điều hướng (`RequestDispatcher` - Tiêm Controllers vào).
6. Mở Port mạng Socket và đưa `Dispatcher` vào hoạt động.