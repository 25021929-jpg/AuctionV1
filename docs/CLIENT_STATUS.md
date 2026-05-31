# CLIENT_STATUS

## Phạm vi bản hiện tại

Bản này chỉ xử lý `Auction-client` và `Auction-shared`. `Auction-server` chưa được refactor/tích hợp lại.

## Đã hoàn thành ở client

1. Client dùng DTO từ `Auction-shared`, không còn package DTO riêng trong `Auction-client`.
2. Auth controller đã được refactor theo hướng MVC hơn:
   - `LoginController` và `RegisterController` không gọi socket trực tiếp.
   - Logic gọi server nằm trong `AuthService` / `AuthServiceImpl`.
3. `UserSession` lưu `UserInfo` an toàn từ shared DTO, không lưu entity server.
4. Sau login, client điều hướng theo role:
   - `ADMIN` -> Admin dashboard
   - `SELLER` -> Seller dashboard
   - role còn lại -> Auction list
5. Action gửi server được gom vào `ActionConstants`; controller/service không hardcode chuỗi action trực tiếp.
6. Bidding client dùng `BigDecimal` cho tiền.
7. `BidServiceImpl` không còn gửi `bidderId = 0`; bidderId được lấy từ `UserSession`.
8. Thêm `DisposableController`; `SceneNavigator` tự gọi `dispose()` controller cũ khi chuyển scene.
9. `LiveBiddingController`, `AuctionListController`, `AuctionDetailController`, `SellerDashboardController`, `AdminDashboardController` cleanup EventBus/socket subscription qua `dispose()`.
10. Auction detail hiển thị đầy đủ hơn: mô tả, danh mục, seller, giá khởi điểm, giá hiện tại, người dẫn đầu, thời gian bắt đầu/kết thúc, trạng thái.
11. Seller dashboard đã được nâng cấp:
    - Không còn dùng `double` trong form giá.
    - Validate tên, mô tả, giá khởi điểm, thời gian bắt đầu/kết thúc.
    - Không còn setDisable lên property đang bind.
    - Delete request không gửi `auctionId = 0` giả; nếu không có auctionId thì gửi `null`.
    - Create/update tự gắn `sellerId` từ `UserSession`.
12. Thêm test cho `MoneyFormat` và `SellerItemFormValidator`.

## Chưa xử lý trong bản này

1. Chưa sửa server để khớp protocol/action mới.
2. Chưa kiểm thử end-to-end với server.
3. Chưa hoàn thiện Forgot Password thật; màn hình hiện vẫn cần nối service/server.
4. Admin dashboard mới ở mức tối thiểu.
5. Chưa bổ sung đầy đủ unit test cho `AuthServiceImpl`, `BidServiceImpl`, `UserSession`.

## Lệnh build nên chạy trên máy có Maven

```bash
mvn -pl Auction-client -am clean test
```

## Ghi chú môi trường

Sandbox hiện không có lệnh `mvn`, nên chưa chạy Maven thật trong môi trường tạo bản vá. Đã kiểm tra XML POM hợp lệ và rà soát các lỗi compile rõ ràng trong phần được sửa.

## Cập nhật Step 3

13. Forgot Password đã được tạm ẩn khỏi luồng đăng nhập vì chưa phải chức năng bắt buộc trong đề.
    - Login view không hiển thị nút quên mật khẩu.
    - `LoginController` không còn điều hướng sang `FORGOT_PASSWORD`.
    - `ScenePaths` và `ActionConstants` không còn expose action/path forgot-password để tránh demo nhầm chức năng chưa hoàn thiện.
14. `BidServiceImpl` đã được inject `ServerCommunicator`, không còn phụ thuộc cứng vào `SocketClient` trong test.
15. Đã bổ sung test cho:
    - `AuthServiceImpl`
    - `BidServiceImpl`
    - `UserSession`

## Việc kế tiếp sau Step 3

1. Chạy Maven để bắt lỗi compile/test thật.
2. Nếu pass, chuyển sang hoàn thiện Admin dashboard tối thiểu và logout/navigation.
3. Sau đó mới khóa contract cuối cùng để chuyển sang server.

## Cập nhật Step 4

16. Đã thêm `ClientSessionManager` để gom thao tác logout/shutdown session:
    - `logoutToLogin()` chỉ clear session và quay về login, không tự đóng socket để tránh tạo lỗi mất kết nối giả.
    - `shutdownApplicationSession()` clear session và disconnect socket khi app đóng thật.
17. `MainClient` đã dùng `SceneNavigator.switchScene(ScenePaths.LOGIN)` ngay từ đầu, giúp navigator nắm controller hiện tại và dispose lifecycle nhất quán hơn.
18. `HomeController` đã hiển thị menu theo role:
    - Bidder: chỉ thấy danh sách phiên đấu giá.
    - Seller: thấy thêm Seller Dashboard.
    - Admin: thấy Seller Dashboard và Admin Dashboard.
19. Đã thêm guard quyền ở Home: người không đúng role không mở được Seller/Admin bằng cách gọi handler trực tiếp.
20. `AdminDashboardController` có thêm nút mở danh sách auction và logout; vẫn giữ vai trò tối thiểu là màn debug realtime/event, chưa đoán thêm API admin server.
21. Đã tạm archive Forgot Password khỏi `src/main`/`src/main/resources` để không còn TODO chức năng chưa bắt buộc trong luồng build/demo.
22. Đã chuyển các file ghi chú không phải `.java` ra khỏi `src/main/java` sang `Auction-client/docs/notes` để source tree sạch hơn.

## Việc kế tiếp sau Step 4

1. Chạy `mvn -pl Auction-client -am clean test` trên máy có Maven.
2. Nếu pass, tiếp tục Step 5: dọn lỗi compile/test nhỏ còn lại, chuẩn hóa error handling và bổ sung tài liệu run/demo client.
3. Sau đó khóa contract cuối cùng và chuyển sang refactor server theo contract.

## Cập nhật Step 5 mini

23. Đã chuẩn hóa error handling mức tối thiểu cho client:
    - Thêm nhóm exception ở `core/error`: `ClientException`, `ConnectionException`, `RequestTimeoutException`, `ServerBusinessException`, `UnauthorizedException`, `ForbiddenException`, `ValidationException`, `InvalidResponseException`.
    - Giữ `ApiException` để tương thích code/test cũ, nhưng gom bản chất vào lỗi nghiệp vụ server.
24. Đã thêm `ErrorHandler` để đổi exception thành thông báo thân thiện, dùng chung cho Alert/Toast.
25. `SocketClient` bắt đầu phân loại lỗi network rõ hơn:
    - chưa kết nối/gửi thất bại -> `ConnectionException`
    - chờ response quá lâu -> `RequestTimeoutException`
    - response rỗng/sai -> `InvalidResponseException`
26. Đã thêm `AccessGuard` để kiểm tra đăng nhập/quyền truy cập màn hình:
    - `requireLogin()`
    - `requireRole(...)`
    - `requireAnyRole(...)`
27. Đã áp dụng guard cho các màn nhạy cảm:
    - `LiveBiddingController`: yêu cầu đăng nhập.
    - `SellerDashboardController`: yêu cầu `SELLER` hoặc `ADMIN`.
    - `AdminDashboardController`: yêu cầu `ADMIN`.
    - `HomeController`: dùng `AccessGuard` khi mở Seller/Admin.
28. Đã thêm test cho:
    - `ErrorHandlerTest`
    - `AccessGuardTest`

## Việc kế tiếp sau Step 5 mini

1. Chạy `mvn -pl Auction-client -am clean test` trên máy có Maven.
2. Nếu pass, khóa tài liệu run/demo + contract client-server.
3. Sau đó chuyển sang xử lý server theo shared DTO/action/protocol mới.

## Cập nhật Step 6

29. Đã khóa client-server contract trong `docs/CLIENT_SERVER_CONTRACT_LOCKED.md`.
30. Đã chuyển nguồn chuẩn action/event sang `Auction-shared`:
    - Canonical: `com.auction.shared.protocol.ActionConstants`
    - Client wrapper cũ `com.auction.client.core.config.ActionConstants` vẫn còn nhưng chỉ là facade tương thích.
31. Đã bổ sung `errorCode` vào `WireMessage` và `Response<T>` để server có thể trả mã lỗi rõ ràng khi migrate.
32. Đã tạo tài liệu chạy client:
    - `docs/CLIENT_RUN_GUIDE.md`
33. Đã tạo checklist demo client:
    - `docs/CLIENT_DEMO_CHECKLIST.md`
34. Đã tạo kế hoạch migrate server sau này:
    - `docs/SERVER_MIGRATION_PLAN.md`
35. Đã đồng bộ lại tài liệu trong `Auction-client/docs` để tránh contract cũ gây nhầm.

## Trạng thái sau Step 6

```text
Auction-shared  ✅ nguồn chuẩn DTO/protocol/action
Auction-client  ✅ đã theo shared contract
Auction-server  ❌ chưa migrate, tạm thời không build trong root
```

## Lệnh build tiếp tục dùng

```bash
mvn -pl Auction-client -am clean test
```

Không chạy root `mvn clean test` cho đến khi xử lý server.
