# Client–Server Contract LOCKED – AuctionV1

Phiên bản này khóa hợp đồng tối thiểu giữa `Auction-client`, `Auction-shared` và `Auction-server`.

> Trạng thái: `Auction-client` và `Auction-shared` đã theo contract này. `Auction-server` **chưa migrate**, nên không chạy root build toàn bộ cho đến khi xử lý server.

---

## 1. Nguồn chuẩn

Các phần sau được xem là nguồn chuẩn, không tự tạo bản trùng ở client/server:

| Nhóm | Package chuẩn |
|---|---|
| Action/Event constants | `com.auction.shared.protocol.ActionConstants` |
| Socket envelope | `com.auction.shared.protocol.WireMessage` |
| Message type | `com.auction.shared.protocol.WireMessageType` |
| Response wrapper | `com.auction.shared.dto.Response<T>` |
| Auth DTO | `com.auction.shared.dto.*`, `com.auction.shared.dto.auth.request.*` |
| Auction DTO | `com.auction.shared.dto.auction.*` |
| Bidding DTO | `com.auction.shared.dto.bidding.*` |
| Seller DTO | `com.auction.shared.dto.seller.*` |
| Role/status enum | `com.auction.shared.domain.*` |

Nguyên tắc: server phải import DTO/action từ `Auction-shared`, không tự định nghĩa lại class trùng tên.

---

## 2. Socket protocol

Mỗi message là **một dòng JSON UTF-8** kết thúc bằng `\n`.

### 2.1 Client request

```json
{
  "type": "REQUEST",
  "requestId": "uuid-string",
  "action": "AUCTION_GET_DETAIL",
  "data": {
    "auctionId": 1
  }
}
```

Quy tắc:

- `type` bắt buộc là `REQUEST`.
- `requestId` bắt buộc, server phải trả lại đúng id này trong response.
- `action` bắt buộc, lấy từ `ActionConstants`.
- `data` là payload DTO đã serialize. Nếu không cần request body, có thể là `null` hoặc `{}`.

### 2.2 Server response

```json
{
  "type": "RESPONSE",
  "requestId": "uuid-string",
  "action": "AUCTION_GET_DETAIL",
  "success": true,
  "message": "OK",
  "errorCode": null,
  "data": {
    "auctionId": 1,
    "itemName": "Laptop",
    "currentPrice": 1500000,
    "status": "RUNNING"
  }
}
```

Quy tắc:

- `type` bắt buộc là `RESPONSE`.
- `requestId` phải khớp request.
- `success=false` thì `message` nên thân thiện và `errorCode` nên là mã máy đọc được.
- `data` là payload thực, không bọc thêm `{success,message,data}` lần nữa bên trong.

### 2.3 Server push event

```json
{
  "type": "EVENT",
  "action": "EVENT_BID_UPDATED",
  "data": {
    "auctionId": 1,
    "newCurrentPrice": 1600000,
    "leaderUsername": "bidder01",
    "bidTime": "2026-05-26T20:30:00"
  }
}
```

Quy tắc:

- `EVENT` không cần `requestId`.
- `action` lấy từ nhóm event trong `ActionConstants`.
- Client map event qua `ServerEventMapper` rồi publish vào `EventBus`.

---

## 3. Action contract

| Action | Request DTO | Response data | Client dùng ở | Ghi chú server |
|---|---|---|---|---|
| `AUTH_LOGIN` | `LoginRequest` | `AuthResponse` | `AuthServiceImpl.login` | Trả `AuthResponse.user` có `id`, `username`, `role` |
| `AUTH_REGISTER` | `RegisterRequest` | `Void` hoặc object rỗng | `AuthServiceImpl.register` | Validate trùng username/email ở server |
| `AUCTION_GET_LIST` | `null` hoặc `{}` | List/array `AuctionSummaryDto` hoặc object chứa `items` | `AuctionServiceImpl.getAuctions` | Client mapper tolerant nhưng nên trả field chuẩn |
| `AUCTION_GET_DETAIL` | `AuctionIdRequest` | `AuctionDetailDto` | `AuctionServiceImpl.getAuctionDetail` | Bắt buộc có `auctionId`, `itemName`, `currentPrice`, `status` |
| `AUCTION_SUBSCRIBE` | `AuctionIdRequest` | `Void` | `BidServiceImpl.subscribe` | Ghi nhận client đang xem auction |
| `AUCTION_UNSUBSCRIBE` | `AuctionIdRequest` | `Void` | `BidServiceImpl.unsubscribe` | Gỡ client khỏi subscription |
| `BID_PLACE_BID` | `PlaceBidRequest` | `BidResultDto` hoặc `Void` | `BidServiceImpl.placeBid` | Sau bid hợp lệ phải broadcast `EVENT_BID_UPDATED` |
| `SELLER_ITEM_LIST_MY` | `null` hoặc seller context từ session | List/array `SellerItemDto` hoặc object chứa `items` | `SellerServiceImpl.getMyItems` | Server nên lấy seller từ session/socket; client vẫn có `sellerId` ở create/update |
| `SELLER_ITEM_CREATE` | `CreateSellerItemRequest` | `Void` hoặc `SellerItemDto` | `SellerServiceImpl.createItem` | Tạo item + auction hoặc theo schema server |
| `SELLER_ITEM_UPDATE` | `UpdateSellerItemRequest` | `Void` hoặc `SellerItemDto` | `SellerServiceImpl.updateItem` | Chặn sửa khi trạng thái không cho phép |
| `SELLER_ITEM_DELETE` | `DeleteSellerItemRequest` | `Void` | `SellerServiceImpl.deleteItem` | `auctionId` có thể null nếu server chỉ cần `itemId` |

---

## 4. Event contract

| Event | Payload DTO | Client xử lý | Ghi chú |
|---|---|---|---|
| `EVENT_BID_UPDATED` | `BidUpdateEventDto` / `BidResultDto` | `LiveBiddingController` | Cập nhật giá hiện tại, leader, chart |
| `EVENT_AUCTION_STATUS_CHANGED` | object có `auctionId`, `status` | Auction/list/live screens | Dùng khi phiên tự đóng hoặc bị hủy |

---

## 5. DTO field tối thiểu

### `AuthResponse`

```json
{
  "user": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "username": "seller01",
    "email": "seller@example.com",
    "phone": "0123456789",
    "dateOfBirth": "2004-01-01",
    "role": "SELLER"
  }
}
```

### `AuctionSummaryDto`

Tối thiểu:

- `auctionId`
- `itemName`
- `startingPrice`
- `currentPrice`
- `startTime`
- `endTime`
- `status`

### `AuctionDetailDto`

Bao gồm field của `AuctionSummaryDto` và nên có thêm:

- `description`
- `categoryName`
- `sellerName`
- `leaderUsername`

### `PlaceBidRequest`

- `auctionId`
- `bidderId`
- `amount`

`auctionSessionId` và `bidAmount` còn tồn tại như alias để giảm rủi ro tích hợp với server cũ, nhưng field chuẩn mới là `auctionId` và `amount`.

### `BidResultDto` / `BidUpdateEventDto`

- `bidId`
- `auctionId`
- `bidderId`
- `bidderUsername`
- `bidAmount`
- `newCurrentPrice`
- `leaderUsername`
- `bidTime`

### Seller DTO

`SellerItemDto` tối thiểu:

- `itemId`
- `auctionId`
- `name`
- `description`
- `categoryId` / `categoryName`
- `startPrice`
- `currentPrice`
- `startTime`
- `endTime`
- `status`

---

## 6. Error code khuyến nghị

Server có thể trả `errorCode` trong `WireMessage`/`Response` để client debug dễ hơn.

| errorCode | Ý nghĩa |
|---|---|
| `AUTH_INVALID_CREDENTIALS` | Sai username/email hoặc password |
| `AUTH_DUPLICATE_USERNAME` | Username đã tồn tại |
| `AUTH_DUPLICATE_EMAIL` | Email đã tồn tại |
| `AUCTION_NOT_FOUND` | Không tìm thấy phiên đấu giá |
| `AUCTION_CLOSED` | Đấu giá khi phiên đã đóng |
| `BID_TOO_LOW` | Giá đặt không cao hơn giá hiện tại |
| `FORBIDDEN` | Không có quyền |
| `VALIDATION_ERROR` | Dữ liệu request không hợp lệ |
| `INTERNAL_ERROR` | Lỗi server không mong muốn |

Client vẫn hiển thị theo `message`; `errorCode` chủ yếu phục vụ debug/test.

---

## 7. Quy tắc không được phá

1. Không hardcode action string trong controller/service; dùng `ActionConstants`.
2. Không tạo DTO trùng ở client/server.
3. Không cho client truy cập database.
4. Không để server trả `Request/Response` cũ không có `type/requestId`, vì client hiện đọc theo `WireMessage`.
5. Không dùng polling liên tục cho live bidding; dùng `EVENT` qua socket.
