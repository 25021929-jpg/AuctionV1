# Client/shared DTO migration

## Đã làm

- Xóa các DTO trùng trong `Auction-client`.
- Chuyển client sang import DTO từ `Auction-shared`:
  - `com.auction.shared.dto.auth.request.*`
  - `com.auction.shared.dto.auction.*`
  - `com.auction.shared.dto.bidding.*`
  - `com.auction.shared.dto.seller.*`
- Cập nhật mapper/service/controller/test phía client để dùng DTO shared.
- Bổ sung alias/backward-compatible methods trong shared DTO để không phá nhiều code UI cũ.

## Chưa làm trong phạm vi này

- Chưa sửa server/protocol socket.
- Chưa đồng bộ action constants với server.
- Chưa chạy Maven do môi trường sandbox không có `mvn`.

## Lệnh cần chạy trên máy có Maven

```bash
mvn -pl Auction-shared clean test
mvn -pl Auction-client -am clean test
```
