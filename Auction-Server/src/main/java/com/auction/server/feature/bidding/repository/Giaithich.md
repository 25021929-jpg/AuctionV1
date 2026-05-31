Dưới đây là bản tóm tắt súc tích, đầy đủ tính khoa học để bạn đưa vào tài liệu giải thích (Documentation/Javadoc) cho hàm `getReference`:

---

### 📋 Tài liệu giải thích: Hàm `getReference` trong Repository

**1. Định nghĩa kỹ thuật:**
Hàm `getReference` sử dụng cơ chế **Lazy Loading (Tải chậm)** của Hibernate để trả về một đối tượng **Proxy** (vật thay thế) thay vì một thực thể (Entity) thực sự được nạp từ Database.

**2. Cơ chế hoạt động (The Proxy Mechanism):**

* **Zero-Hit Database:** Khi gọi hàm này, Hibernate **không** thực thi lệnh `SELECT`. Nó chỉ tạo ra một đối tượng "rỗng" trong bộ nhớ.
* **Placeholder:** Đối tượng này chỉ được lấp đầy duy nhất một thông tin là `ID`. Tất cả các thuộc tính khác (như username, email,...) đều ở trạng thái chưa khởi tạo.
* **Lazy Initialization:** Database chỉ được truy vấn khi và chỉ khi bạn gọi các hàm getter để lấy dữ liệu khác ngoài ID (ví dụ: `user.getName()`).

**3. Mục đích sử dụng & Tối ưu hiệu năng:**

* **Thiết lập quan hệ (Foreign Key):** Dùng khi bạn cần gắn một thực thể vào một thực thể khác (ví dụ: gán `Bidder` vào `Bid`). Vì Database chỉ cần cái `ID` để làm khóa ngoại, việc nạp toàn bộ thông tin User là dư thừa.
* **Tiết kiệm tài nguyên:** Giảm thiểu số lượng câu lệnh `SELECT` không cần thiết, giảm băng thông mạng và tải trọng cho Database Server.

**4. So sánh với `findById`:**

| Đặc điểm | `findById(id)` | `getReference(id)` |
| --- | --- | --- |
| **Truy vấn DB** | Chạy ngay lập tức (`SELECT...`) | Không chạy (chỉ tạo Proxy) |
| **Kết quả** | Đối tượng thật (đầy đủ dữ liệu) | Đối tượng Proxy (chỉ có ID) |
| **Trường hợp dùng** | Khi cần đọc/hiển thị dữ liệu | Khi chỉ cần ID để lưu khóa ngoại |
| **Lỗi có thể gặp** | Trả về `null` hoặc `Optional.empty()` | `EntityNotFoundException` (khi dùng Proxy lỗi) |

**5. Ví dụ áp dụng trong Doctype:**

> *"Sử dụng `getReference` khi cần tham chiếu đến một thực thể đã tồn tại để thực hiện các thao tác ghi (Insert/Update) mà không có nhu cầu truy xuất thông tin chi tiết của thực thể đó, nhằm tối ưu hóa hiệu suất hệ thống."*

---
