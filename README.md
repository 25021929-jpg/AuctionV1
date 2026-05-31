# AuctionV1 - Hệ thống đấu giá trực tuyến

## 1. Mô tả bài toán và phạm vi hệ thống
AuctionV1 là hệ thống đấu giá trực tuyến theo kiến trúc Client-Server. Người dùng có thể đăng ký/đăng nhập với vai trò Bidder, Seller hoặc Admin; Seller quản lý sản phẩm/phiên đấu giá; Bidder xem danh sách phiên, đặt giá và theo dõi cập nhật realtime; Server quản lý dữ liệu, trạng thái phiên, lịch sử bid, ví và thanh toán.

## 2. Công nghệ sử dụng
- Ngôn ngữ: Java 17
- Client GUI: JavaFX, FXML, CSS
- Giao tiếp mạng: Java Socket TCP, JSON line protocol, Gson
- Server/Data: MySQL, Hibernate, HikariCP
- Build tool: Maven multi-module
- Test: JUnit 5, Mockito
- CI/CD: GitHub Actions
- Coding convention: Checkstyle, Spotless/Google Java Format

## 3. Cấu trúc module chính
```text
AuctionV1/
├── Auction-shared/   # DTO, enum domain, protocol WireMessage, validation dùng chung
├── Auction-server/   # Socket server, controller-service-repository, entity, DB schema
├── Auction-client/   # JavaFX client, feature-based UI, service, network, EventBus
├── .github/workflows/ci.yml
└── README.md
```

## 4. Vị trí file JAR
Sau khi build Maven, file JAR nằm tại:
```text
Auction-server/target/Auction-server-1.0-SNAPSHOT.jar
Auction-client/target/Auction-client-1.0-SNAPSHOT.jar
Auction-shared/target/Auction-shared-1.0-SNAPSHOT.jar
```
Lưu ý khi nộp cuối: cần kiểm tra lại JAR đã là executable fat JAR/uber JAR hoặc đã cấu hình manifest/main class phù hợp để chạy trực tiếp bằng `java -jar`.

## 5. Cài đặt môi trường
1. Cài JDK 17.
2. Cài Maven.
3. Cài MySQL, tạo database bằng file:
```text
Auction-server/src/main/resources/sql/schema.sql
Auction-server/src/main/resources/sql/data.sql
```
4. Kiểm tra cấu hình DB trong:
```text
Auction-server/src/main/resources/application.properties
```
5. Mặc định server chạy port `8888`, client kết nối qua cấu hình trong `AppConfig/config.properties`.

## 6. Hướng dẫn build
Tại thư mục gốc project:
```bash
mvn clean package
```
Chạy test:
```bash
mvn test
```

## 7. Hướng dẫn chạy Server/Client
Chạy Server trước:
```bash
java -jar Auction-server/target/Auction-server-1.0-SNAPSHOT.jar
```
Hoặc trong IDE chạy class:
```text
com.auction.server.MainServer
```

Sau đó mở một hoặc nhiều Client:
```bash
java -jar Auction-client/target/Auction-client-1.0-SNAPSHOT.jar
```
Hoặc dùng Maven/IDE:
```bash
mvn -pl Auction-client javafx:run
```

## 8. Danh sách chức năng đã hoàn thành
- Đăng ký / đăng nhập người dùng.
- Phân quyền vai trò Admin / Seller / Bidder.
- Danh sách phiên đấu giá.
- Chi tiết phiên đấu giá.
- Live bidding và đặt giá.
- Seller quản lý sản phẩm/phiên đấu giá.
- Wallet: nạp tiền, xem số dư, xem lịch sử giao dịch.
- Server xử lý nhiều client bằng socket.
- Realtime update qua server push event, EventBus và AuctionRoomRegistry.
- Scheduler cập nhật trạng thái phiên đấu giá.
- Xử lý lỗi nhập liệu, lỗi kết nối, timeout và lỗi nghiệp vụ.
- Unit test cho logic quan trọng ở client/server/shared.
- CI/CD GitHub Actions build/test tự động.

## 9. Link báo cáo PDF và video demo
- Báo cáo PDF: https://drive.google.com/drive/folders/1EbBB7q815Guuul6l_dmG8fQnslC9sp8u?usp=drive_link
## 10. Tài khoản demo
Cập nhật theo `data.sql` sau khi chạy seed database:
```text
Admin:  admin / <mật khẩu demo>
Seller: seller01 / <mật khẩu demo>
Bidder: bidder01 / <mật khẩu demo>
```