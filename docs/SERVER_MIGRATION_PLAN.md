# Server Migration Plan – Sau khi client/shared đã khóa contract

Tài liệu này chỉ là kế hoạch. Bản Step 6 **không sửa server**.

---

## Mục tiêu

Migrate `Auction-server` để khớp `Auction-shared` và `Auction-client` hiện tại, không sửa ngược client theo server cũ.

---

## Thứ tự nên làm

### Step S1 – Server compile pass với shared mới

- Sửa import DTO cũ sang DTO trong `Auction-shared`.
- Xóa/không dùng DTO trùng trong server nếu có.
- Sửa lỗi class thiếu như `BidResultDto` bằng cách import shared DTO.

### Step S2 – Socket protocol WireMessage

- `ClientHandler` đọc từng dòng JSON.
- Deserialize thành `WireMessage`.
- Route theo `action`.
- Response phải là `WireMessage type=RESPONSE` với cùng `requestId`.
- Event phải là `WireMessage type=EVENT`.

### Step S3 – RequestRouter + Controller

- `RequestRouter` map action sang controller.
- Controller không viết SQL.
- Controller gọi service.

### Step S4 – Auth

- `AUTH_LOGIN`: nhận `LoginRequest`, trả `AuthResponse`.
- `AUTH_REGISTER`: nhận `RegisterRequest`, validate trùng user/email.
- Sau login, server nên lưu session theo socket hoặc trả thông tin đủ để client set `UserSession`.

### Step S5 – Auction

- `AUCTION_GET_LIST`: trả list `AuctionSummaryDto`.
- `AUCTION_GET_DETAIL`: trả `AuctionDetailDto`.
- Chỉ server đọc database.

### Step S6 – Bidding + concurrency

- `BID_PLACE_BID`: nhận `PlaceBidRequest`.
- Lock theo `auctionId` hoặc transaction DB để tránh race condition.
- Validate auction running, bid > current price.
- Lưu `BidTransaction`.
- Cập nhật current price/leader.
- Broadcast `EVENT_BID_UPDATED` cho các client subscribe.

### Step S7 – Seller

- `SELLER_ITEM_LIST_MY`.
- `SELLER_ITEM_CREATE`.
- `SELLER_ITEM_UPDATE`.
- `SELLER_ITEM_DELETE`.
- Validate quyền seller/admin và trạng thái auction.

### Step S8 – Lifecycle + realtime status

- Tự đóng phiên hết hạn.
- Broadcast `EVENT_AUCTION_STATUS_CHANGED`.

### Step S9 – DAO/MySQL

- `UserDao`, `ItemDao`, `AuctionDao`, `BidTransactionDao`.
- Service không viết SQL trực tiếp.
- Client không truy cập DB.

### Step S10 – Test server

- Unit test service: auth, bid validate, auction close.
- Test concurrent bid.
- Test mapper DTO.

---

## Không nên làm

- Không sửa client quay lại protocol `Request/Response` cũ.
- Không để server trả response thiếu `requestId`.
- Không dùng polling realtime.
- Không truy cập database từ client.
