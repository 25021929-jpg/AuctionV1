# Đề xuất cấu trúc server hoàn chỉnh cho AuctionV1

Mục tiêu của server: chỉ server truy cập database, client giao tiếp qua Socket JSON, xử lý concurrency/realtime/update theo đúng yêu cầu bài tập lớn.

## 1. Cấu trúc thư mục đề xuất

```text
Auction-server/src/main/java/com/auction/server
├── MainServer.java
├── config
│   ├── ServerConfig.java
│   ├── DatabaseConfig.java
│   └── GsonConfig.java
├── network
│   ├── AuctionSocketServer.java
│   ├── ClientHandler.java
│   ├── ClientRegistry.java
│   ├── RequestRouter.java
│   └── SubscriptionManager.java
├── controller
│   ├── AuthController.java
│   ├── AuctionController.java
│   ├── BidController.java
│   ├── SellerController.java
│   └── AdminController.java
├── service
│   ├── AuthService.java
│   ├── AuctionService.java
│   ├── BidService.java
│   ├── SellerService.java
│   ├── AuctionLifecycleService.java
│   ├── RealtimeNotificationService.java
│   └── AutoBidService.java
├── dao
│   ├── UserDao.java
│   ├── ItemDao.java
│   ├── AuctionDao.java
│   ├── BidTransactionDao.java
│   ├── AutoBidDao.java
│   └── JdbcUtils.java
├── model
│   ├── UserEntity.java
│   ├── ItemEntity.java
│   ├── AuctionEntity.java
│   ├── BidTransactionEntity.java
│   └── AutoBidEntity.java
├── mapper
│   ├── UserMapper.java
│   ├── AuctionMapper.java
│   ├── SellerItemMapper.java
│   └── BidMapper.java
├── exception
│   ├── ServerException.java
│   ├── AuthenticationException.java
│   ├── AuthorizationException.java
│   ├── InvalidBidException.java
│   ├── AuctionClosedException.java
│   └── DataAccessException.java
└── security
    ├── PasswordHasher.java
    ├── SessionManager.java
    └── RoleGuard.java
```

## 2. Nguyên tắc phân tầng

- `network`: chỉ nhận/gửi `WireMessage`, không chứa nghiệp vụ.
- `controller`: parse DTO từ shared, gọi service, trả `Response<T>`.
- `service`: chứa nghiệp vụ chính: login, CRUD sản phẩm, đặt giá, đóng phiên, realtime, concurrency.
- `dao`: SQL/JDBC/MySQL. Chỉ DAO được truy cập database.
- `mapper`: đổi entity database sang DTO trong `Auction-shared`.
- `shared`: là hợp đồng chung: DTO, enum, protocol, validation, exception cơ bản.

## 3. Luồng request chuẩn

```text
Client SocketClient
  -> WireMessage(type=REQUEST, requestId, action, data)
  -> ClientHandler
  -> RequestRouter
  -> Controller theo action
  -> Service
  -> DAO/MySQL
  -> Response<T>
  -> WireMessage(type=RESPONSE, requestId, action, data=response)
  -> Client
```

Realtime event:

```text
BidService.placeBid() thành công
  -> RealtimeNotificationService.broadcastBidUpdated(...)
  -> SubscriptionManager tìm các client đang xem auctionId
  -> WireMessage(type=EVENT, action=EVENT_BID_UPDATED, data=BidUpdateEventDto)
  -> toàn bộ client đang xem phiên cập nhật UI
```

## 4. Controller/action nên có

```text
AuthController
├── AUTH_LOGIN
├── AUTH_REGISTER
├── AUTH_FORGOT_PASSWORD
├── AUTH_VERIFY_OTP
└── AUTH_RESET_PASSWORD

AuctionController
├── AUCTION_GET_LIST
├── AUCTION_GET_DETAIL
├── AUCTION_SUBSCRIBE
└── AUCTION_UNSUBSCRIBE

BidController
└── BID_PLACE_BID

SellerController
├── SELLER_LIST_MY_ITEMS
├── SELLER_CREATE_ITEM
├── SELLER_UPDATE_ITEM
└── SELLER_DELETE_ITEM

AdminController
├── ADMIN_LIST_USERS
├── ADMIN_LOCK_USER
├── ADMIN_CANCEL_AUCTION
└── ADMIN_VIEW_LOGS
```

Toàn bộ tên action nên đưa vào `Auction-shared`, ví dụ `com.auction.shared.protocol.Action`, để client/server không lệch string.

## 5. Concurrency cho đặt giá

`BidService.placeBid()` phải là critical section theo từng `auctionId`, không lock toàn server.

Gợi ý:

```text
ConcurrentHashMap<Long, ReentrantLock> auctionLocks
```

Luồng xử lý:

1. Lock theo `auctionId`.
2. Load auction mới nhất từ DB.
3. Kiểm tra trạng thái còn biddable.
4. Kiểm tra amount > currentPrice.
5. Insert `BidTransaction`.
6. Update currentPrice + leader.
7. Commit transaction DB.
8. Unlock.
9. Broadcast event realtime.

Tuyệt đối không broadcast trước khi commit DB thành công.

## 6. Database tối thiểu

```text
users(id, full_name, username, email, phone, date_of_birth, password_hash, role, status, created_at)
items(id, seller_id, category_id, name, description, starting_price, created_at, updated_at)
auctions(id, item_id, start_time, end_time, current_price, leader_user_id, status, created_at, updated_at)
bid_transactions(id, auction_id, bidder_id, amount, bid_time)
auto_bids(id, auction_id, bidder_id, max_bid, increment, priority_time, active)
```

## 7. Thứ tự triển khai server sau khi client/shared ổn

1. Chuẩn hóa `WireMessage` và `Action` trong shared.
2. Sửa `ClientHandler` đọc/ghi đúng `WireMessage`.
3. Viết `RequestRouter` map action -> controller.
4. Hoàn thiện `AuthController/AuthService/UserDao`.
5. Hoàn thiện `AuctionController/AuctionService/AuctionDao`.
6. Hoàn thiện `BidController/BidService` có lock theo auction.
7. Thêm `SubscriptionManager` và broadcast realtime event.
8. Thêm Seller CRUD.
9. Thêm lifecycle scheduler tự đóng phiên hết hạn.
10. Sau khi core xanh mới làm auto-bidding, anti-sniping, chart realtime.
```
