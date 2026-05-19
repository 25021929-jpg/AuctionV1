# Đánh giá và nhận xét về `Auction-Server`

Tệp này tổng hợp các nhận xét, đánh giá và đề xuất cải tiến cho module `Auction-Server` dựa trên phân tích mã nguồn hiện có trong project.

---

## 1. Tổng quan kiến trúc

- Module chính: `com.auction.server`
  - `database.DatabaseConnection` — đọc `application.properties` và mở kết nối JDBC
  - `feature.*` — tổ chức theo lớp: `repository` (DB access), `service` (business), `controller` (API layer)
    - `feature.auth` — authentication: `AuthService`, `UserRepository`, `PasswordResetRepository`, `PasswordUtil`, `ResetTokenUtil`, `AuthController`
    - `feature.auction` — auction management: `AuctionService`, `AuctionItemRepository`, `AuctionSessionRepository`, `AuctionController`
    - `feature.bidding` — hiện thiếu phần xử lý chính (controller rỗng)
  - `exception.DataAccessException` — wrapper cho lỗi truy cập dữ liệu
  - `network` — custom TCP server:
    - `ServerSocketManager` — lắng nghe port, accept socket, spawn `ClientHandler` per connection
    - `ClientHandler` — đọc dòng JSON (per-line), dùng Gson parse `Request`, gửi vào `RequestDispatcher`, trả `Response` JSON
    - `RequestDispatcher` — switch-case trên `action` (ví dụ `AUTH_LOGIN`, `AUTH_REGISTER`) để gọi controller tương ứng
- Shared DTOs: `Auction-shared` chứa `Request` và `Response<T>` để đồng bộ giữa client & server

---

## 2. Điểm mạnh

1. Cấu trúc rõ ràng: tách repository, service, controller — dễ hiểu và bảo trì.
2. Sử dụng PreparedStatement cho truy vấn SQL → giảm rủi ro SQL injection.
3. Password hashing tốt: PBKDF2WithHmacSHA256 + salt + so sánh an toàn (constant-time) — rất tốt cho bảo mật mật khẩu.
4. Quản lý resource tốt: try-with-resources khi sử dụng JDBC.
5. Có `Response` wrapper chuẩn giúp chuẩn hoá API trả về cho client.
6. Shared module (`Auction-shared`) để chia sẻ DTO/validation giữa client/server — thuận tiện.

---

## 3. Vấn đề và điểm cần cải thiện (chi tiết)

### A. Database & Connection
- `DatabaseConnection.getConnection()` dùng `DriverManager.getConnection(...)` mỗi lần cần kết nối — không có connection pool.
  - Ảnh hưởng: overhead tạo kết nối, giảm hiệu năng khi nhiều request.
  - Khuyến nghị: dùng connection pool (HikariCP) và cấu hình pool trong `application.properties` hoặc external config.
- `application.properties` chứa credentials ở dạng plain-text trong repo.
  - Khuyến nghị: không commit credentials; dùng biến môi trường hoặc secret management.

### B. Logging & Observability
- Hiện chỉ dùng `System.out.println` để log.
  - Khuyến nghị: tích hợp SLF4J + Logback/Log4J2, log ở các mức (DEBUG/INFO/WARN/ERROR), log exception stacktrace.
- Thiếu metrics/health endpoints (nếu cần chạy production).

### C. Concurrency & Threading
- `ServerSocketManager` tạo `new Thread(clientHandler).start()` cho mỗi client.
  - Ảnh hưởng: không scale khi nhiều client; RAM/threads tăng gây OOM.
  - Khuyến nghị: thay bằng `ExecutorService` (thread pool) hoặc chuyển sang non-blocking NIO/Netty cho nhu cầu cao.

### D. Bảo mật giao tiếp
- Giao tiếp qua plain TCP (không mã hoá) → dữ liệu nhạy cảm (mật khẩu, email) truyền rõ.
  - Khuyến nghị: dùng TLS/SSL (SSLSocket) hoặc chuyển sang HTTPs/REST với TLS; hoặc dùng SSH tunnel.

### E. Xác thực & phân quyền
- Sau khi `login`, server không issue token/session.
  - Ảnh hưởng: client cần gửi credentials mỗi lần hoặc không có cơ chế xác thực an toàn.
  - Khuyến nghị: issue JWT hoặc session token, client đính token trong mỗi request.

### F. Real-time auction / bidding
- Hiện chưa có cơ chế push/broadcast events tới nhiều client (cần cho bidding realtime).
  - Khuyến nghị: dùng WebSocket (WSS) hoặc giữ TCP nhưng thêm subscription manager và broadcast mechanism.

### G. Error handling
- `AuthController` trả `Response.fail("Internal server error")` cho exception chung — tốt cho client nhưng thiếu log chi tiết server-side.
  - Khuyến nghị: log stacktrace, phân biệt lỗi hệ thống vs lỗi nghiệp vụ, có mã lỗi (error code) hợp lý.

---

## 4. Vấn đề cụ thể tìm thấy trong mã

- `AuctionSessionRepository.findAll()` và `findDetailById()` map `Timestamp.toLocalDateTime()` — cần chú ý timezone giữa DB và JVM.
- `UserRepository.existsByUsername`/`existsByEmail` dùng `SELECT id FROM users WHERE ...` rồi `rs.next()` — OK, nhưng `SELECT COUNT(*)` là lựa chọn khác.
- `PasswordResetRepository.saveToken()` không kiểm tra duplicate token (không bắt buộc nhưng có thể thêm unique constraint trên `token`).
- `feature.bidding.controller.BidController` hiện trống — phần bidding chưa được triển khai.

---

## 5. Đề xuất cải tiến (ưu tiên)

1. (Ưu tiên cao) Thay DriverManager bằng connection pool (HikariCP). Cấu hình pool in `application.properties`.
2. (Cao) Thay model thread-per-connection bằng `ExecutorService` với fixed/thread pool hoặc migrate lên Netty/Vert.x nếu cần 1000+ kết nối.
3. (Cao) Thêm TLS cho socket (SSLSocket) hoặc chuyển sang HTTP(S) API.
4. (Trung) Thêm authentication token (JWT) ở endpoint login; validate token với mỗi request.
5. (Trung) Thêm logging framework (SLF4J + Logback) và log exception stacktrace.
6. (Trung) Thêm health / metrics endpoints (ví dụ HTTP nhỏ hoặc JMX) cho monitoring.
7. (Trung) Implement cơ chế broadcast/subscribe cho real-time bidding (WebSocket hoặc publish/subscribe qua server socket manager).
8. (Thấp) Di chuyển DB credentials ra khỏi mã nguồn (env vars / .env / vault).

---

## 6. Cách kết nối `Auction-client` với `Auction-Server` (hiện trạng, không sửa code)

1. Kiểm tra config:
   - Client trong repo dùng `new Socket("localhost", 8888)` (xem `Auction-client/src/.../SocketClient.java`).
   - Server mặc định cần lắng nghe cùng port (trong `ServerSocketManager` bạn đang truyền `port` khi khởi). Hãy đảm bảo server start ở port 8888 hoặc cập nhật client.

2. Chạy DB & import schema/data
   - Chạy MySQL (hoặc DB đã config trong `Auction-Server/src/main/resources/application.properties`)
   - Import `schema.sql` và `data.sql` nếu cần (nằm trong resources/sql)

3. Khởi server
   - Nếu project có class main/server runner: chạy server (IDE hoặc `mvn -pl Auction-Server javafx:run` tuỳ cấu hình)
   - Nếu không có main, bạn cần tạo tạm một Main để gọi `new ServerSocketManager(8888).start();` (nếu bạn muốn tôi tạo giúp, tôi có thể thêm file `MainServer.java`)

4. Trên client (IDE): gọi `SocketClient.connect()` để kết nối, sau đó gửi request JSON theo cấu trúc `Request` (action + body JSON).
   - Ví dụ tạo `RegisterRequest rr = ...; bodyJson = gson.toJson(rr); Request req = new Request("AUTH_REGISTER", bodyJson); String reqJson = gson.toJson(req);` gửi `reqJson`.
   - Server `ClientHandler` đọc 1 dòng JSON, parse, dispatch và trả về JSON `Response`.

5. Chú ý:
   - Luôn thực hiện network call trong background thread, không block JavaFX UI thread.
   - `Request.body` ở server được parse bằng `gson.fromJson(body, RegisterRequest.class)`; vậy client cần set `body` chính là JSON string của request object.

---

## 7. Test nhanh & checklist kiểm thử

- [ ] DB đã được khởi và schema import thành công.
- [ ] Server khởi và lắng nghe port (ví dụ 8888).
- [ ] Client `SocketClient.connect()` kết nối thành công (console: Connected to server).
- [ ] Gửi request đăng ký (AUTH_REGISTER) và nhận `Response.success`.
- [ ] Gửi login (AUTH_LOGIN) và nhận thông tin `AuthResponse`.
- [ ] Thử tình huống lỗi (invalid JSON) và quan sát `Response.fail` từ server và logs server-side.

---

## 8. Gợi ý thêm (kĩ thuật nâng cao)

- Xây layer API HTTP nhanh bằng Spring Boot (Spring Web) để dễ thử nghiệm và tích hợp (REST endpoints) — vẫn giữ shared DTOs.
- Sử dụng WebSocket (Spring WebSocket / Socket.IO / Netty) cho realtime bidding (broadcast event: new bid, auction ended).
- Bảo mật: triển khai rate limiting cho endpoints đăng nhập/forgot-password để chống brute-force.
- Migration DB: dùng Flyway hoặc Liquibase để quản lý schema thay vì script thủ công.

---

## 9. Kết luận ngắn gọn

- `Auction-Server` có nền tảng tốt (cấu trúc rõ ràng, xử lý password an toàn, prepared statements). Tuy nhiên cần cải thiện ở các mặt: connection pooling, logging, bảo mật kênh giao tiếp (TLS), thread model để có thể mở rộng và an toàn hơn trong môi trường production. Nếu mục tiêu là ứng dụng nhiều client realtime (bidding), cần thêm cơ chế push/broadcast (WebSocket) và quản lý session/token.

---

Nếu bạn muốn, tôi có thể tiếp tục và thực hiện một hoặc nhiều việc sau (chọn 1 hoặc nhiều):

- [ ] Tạo class `MainServer.java` để bạn khởi server dễ dàng (cùng port 8888) — không sửa logic hiện có.
- [ ] Thêm HikariCP configuration và chuyển `DatabaseConnection` để dùng pool.
- [ ] Thay `System.out.println` bằng SLF4J + Logback cơ bản và cập nhật logging calls.
- [ ] Thêm JWT issuance ở `AuthService.login()` và validate token ở `RequestDispatcher`.
- [ ] Implement thread pool (`ExecutorService`) cho `ServerSocketManager` thay vì `new Thread`.

Hãy cho tôi biết bạn muốn tôi bắt đầu với mục nào, tôi sẽ thực hiện trực tiếp trong code và chạy kiểm thử.
