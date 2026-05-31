# CLIENT_SERVER_CONTRACT_DRAFT

Đây là bản contract phía client đang gọi. Khi xử lý server, nên bám theo bảng này để tránh lệch action/DTO.

| Nhóm | Action | Request DTO | Response/Event DTO | Client dùng ở đâu |
|---|---|---|---|---|
| Auth | `AUTH_LOGIN` | `LoginRequest` | `AuthResponse` | `AuthServiceImpl.login` |
| Auth | `AUTH_REGISTER` | `RegisterRequest` | `Void` | `AuthServiceImpl.register` |
| Auth optional | Forgot password / OTP / Reset password | Tạm ẩn | Tạm ẩn | Không nằm trong demo bắt buộc hiện tại |
| Auction | `AUCTION_GET_LIST` | `null` hoặc filter sau này | `JsonElement` mapped sang `List<AuctionSummaryDto>` | `AuctionServiceImpl.fetchAuctions` |
| Auction | `AUCTION_GET_DETAIL` | `AuctionIdRequest` | `JsonElement` mapped sang `AuctionDetailDto` | `AuctionServiceImpl.fetchAuctionDetail` |
| Auction realtime | `AUCTION_SUBSCRIBE` | `AuctionIdRequest` | `Void` | `BidServiceImpl.subscribeAuction` |
| Auction realtime | `AUCTION_UNSUBSCRIBE` | `AuctionIdRequest` | `Void` | `BidServiceImpl.unsubscribeAuction` |
| Bidding | `BID_PLACE_BID` | `PlaceBidRequest` | `Void` hoặc `BidResultDto` về sau | `BidServiceImpl.placeBid` |
| Seller | `SELLER_ITEM_LIST_MY` | `null` | `JsonElement` mapped sang `List<SellerItemDto>` | `SellerServiceImpl.listMyItems` |
| Seller | `SELLER_ITEM_CREATE` | `CreateSellerItemRequest` | `Void` | `SellerServiceImpl.createItem` |
| Seller | `SELLER_ITEM_UPDATE` | `UpdateSellerItemRequest` | `Void` | `SellerServiceImpl.updateItem` |
| Seller | `SELLER_ITEM_DELETE` | `DeleteSellerItemRequest` | `Void` | `SellerServiceImpl.deleteItem` |
| Server event | `EVENT_BID_UPDATED` | server push | `BidUpdateEventDto`/JSON | `LiveBiddingController` |
| Server event | `EVENT_AUCTION_STATUS_CHANGED` | server push | JSON `{auctionId,status}` | `LiveBiddingController` |

## Protocol khuyến nghị

Client hiện dùng `WireMessage`:

```text
REQUEST:  { type, requestId, action, data }
RESPONSE: { type, requestId, action, success, message, data }
EVENT:    { type, action, data }
```

Server nên dùng cùng `WireMessage` trong `Auction-shared` để tránh client timeout do không match `requestId`.
