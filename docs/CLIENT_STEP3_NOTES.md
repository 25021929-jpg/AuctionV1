# CLIENT STEP 3 NOTES

## Phạm vi

Bản này tiếp tục xử lý trong phạm vi `Auction-client` và `Auction-shared`, chưa sửa server.

## Thay đổi chính

### 1. Tạm ẩn Forgot Password

Forgot Password không nằm trong nhóm chức năng bắt buộc của đề nên đã tạm ẩn khỏi luồng demo.

Đã xử lý:

- Không còn nút/link điều hướng Forgot Password trong màn login.
- `LoginController` không còn method điều hướng sang `FORGOT_PASSWORD`.
- `ScenePaths` không expose `FORGOT_PASSWORD` nữa.
- `ActionConstants` không expose `AUTH_FORGOT_PASSWORD`, `AUTH_VERIFY_OTP`, `AUTH_RESET_PASSWORD` nữa.

Các file controller/FXML/validator cũ của forgot password vẫn được giữ lại trong source để có thể bật lại sau nếu nhóm muốn làm nâng cao, nhưng hiện không còn được nối vào flow chính.

### 2. Tăng testability cho BidService

`BidServiceImpl` giờ nhận `ServerCommunicator` qua constructor.

Mục tiêu:

- Test service mà không cần socket/server thật.
- Giữ controller/service đúng hướng MVC hơn.
- Hạn chế phụ thuộc static `SocketClient` trong logic nghiệp vụ.

### 3. Bổ sung unit test

Thêm các test:

- `AuthServiceImplTest`
- `BidServiceImplTest`
- `UserSessionTest`
- `FakeServerCommunicator` trong `src/test/java/.../testsupport`

Các test này kiểm tra phần logic client quan trọng mà không cần JavaFX runtime hoặc server thật.

## Lệnh cần chạy

```bash
mvn -pl Auction-client -am clean test
```

Nếu có lỗi compile tiếp theo, ưu tiên sửa theo thứ tự:

1. lỗi DTO/import/type mismatch
2. lỗi test constructor/field
3. lỗi JavaFX module path
4. lỗi do server chưa khớp protocol/action
