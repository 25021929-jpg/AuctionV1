# Client Run Guide – AuctionV1

Tài liệu này mô tả cách build/chạy phần `Auction-client` và `Auction-shared` trong giai đoạn server chưa migrate.

---

## 1. Yêu cầu môi trường

- JDK theo cấu hình Maven của project.
- Maven hoặc Maven tích hợp trong IntelliJ.
- IntelliJ IDEA có thể dùng để chạy JavaFX qua Maven.

---

## 2. Build đúng phạm vi hiện tại

Hiện tại chỉ build `shared + client`:

```bash
mvn -pl Auction-client -am clean test
```

Ý nghĩa:

- `-pl Auction-client`: build module client.
- `-am`: tự build module phụ thuộc, tức `Auction-shared`.

Có thể build riêng shared:

```bash
mvn -pl Auction-shared clean test
```

---

## 3. Chưa chạy root build toàn bộ

Không dùng lệnh này ở thời điểm hiện tại:

```bash
mvn clean test
```

Lý do: lệnh root sẽ build cả `Auction-server`, trong khi server chưa được migrate theo DTO/action/protocol mới.

Trạng thái hiện tại:

```text
Auction-shared  ✅ build OK
Auction-client  ✅ build OK
Auction-server  ❌ chưa migrate, tạm bỏ qua
```

---

## 4. Chạy client trong IntelliJ

Cách khuyến nghị:

1. Mở project từ thư mục chứa `pom.xml` cha.
2. Reload Maven project.
3. Tạo Maven run configuration:

```text
Working directory: <thư mục gốc AuctionV1_login>
Command line: -pl Auction-client -am javafx:run
```

Nếu dùng class `MainClient` để chạy trực tiếp, cần đảm bảo JavaFX module path do Maven/IDE cấu hình đúng.

---

## 5. Khi cần build client nhưng bị dùng shared cũ

Nếu gặp lỗi kiểu constructor/field DTO không khớp dù source đã đúng, xóa bản SNAPSHOT cũ:

```text
C:\Users\MY PC\.m2\repository\com\auction\Auction-shared
```

Sau đó chạy lại:

```bash
mvn -pl Auction-client -am clean test
```

---

## 6. Chức năng client đã có ở mức UI/service

- Login/register qua `AuthService`.
- Lưu session bằng `UserSession`.
- Điều hướng theo role: Bidder/Seller/Admin.
- Auction list/detail.
- Live bidding UI + subscribe/unsubscribe event lifecycle.
- Seller dashboard CRUD form + validate.
- Admin dashboard tối thiểu.
- Logout/session cleanup.
- Error handling và access guard cơ bản.

---

## 7. Chức năng đang chờ server

- Login/register thật với database.
- Load auction list/detail thật.
- Bid thật và broadcast realtime.
- Seller CRUD thật.
- Tự đóng phiên đấu giá.
- Concurrency ở server.
- MySQL/DAO.
