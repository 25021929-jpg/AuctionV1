# CLIENT STEP 4 NOTES

## Mục tiêu

Hoàn thiện các phần nền tảng còn lại của client trước khi chuyển sang server:

- Logout/session lifecycle.
- Role-based home menu.
- Admin dashboard tối thiểu.
- Dọn các file/TODO không thuộc chức năng bắt buộc ra khỏi source chính.

## Thay đổi chính

### 1. ClientSessionManager

File mới:

```text
Auction-client/src/main/java/com/auction/client/core/session/ClientSessionManager.java
```

Trách nhiệm:

- `logoutToLogin()`: clear `UserSession`, quay lại Login.
- `shutdownApplicationSession()`: clear `UserSession`, disconnect `SocketClient` khi app đóng.

Logout không tự disconnect socket vì đây là thao tác người dùng chủ động; disconnect có thể làm `SocketClient.readLoop()` phát `CONNECTION_LOST` giả.

### 2. MainClient

`MainClient` không còn tự load FXML thủ công bằng `FXMLLoader` ở màn đầu. Thay vào đó dùng:

```java
SceneNavigator.switchScene(ScenePaths.LOGIN);
```

Điều này giúp `SceneNavigator` quản lý lifecycle controller nhất quán từ màn đầu tiên.

### 3. Home theo role

`HomeController` giờ đọc `UserSession` và chỉ hiện chức năng phù hợp:

- `BIDDER`: Auction list.
- `SELLER`: Auction list + Seller dashboard.
- `ADMIN`: Auction list + Seller dashboard + Admin dashboard.

### 4. Admin dashboard tối thiểu

Admin dashboard hiện vẫn không đoán API server. Màn này dùng để:

- Theo dõi server event/realtime.
- Mở danh sách auction.
- Logout.

### 5. Archive chức năng chưa bắt buộc

Forgot Password đã được tạm đưa khỏi `src/main` vì chưa phải yêu cầu bắt buộc và chưa có backend/service hoàn chỉnh.

Các file ghi chú không phải source Java cũng được chuyển sang `Auction-client/docs/notes`.
