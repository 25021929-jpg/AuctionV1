# AuctionV1 - Tong quan luong chay va kien truc he thong dau gia

Tai lieu nay giai thich cach he thong `AuctionV1` dang hoat dong o muc client-server, cac class/DTO lien quan, luong request tu UI den database, cac quyet dinh thiet ke da chon, diem manh va nhung diem can cai thien ve sau.

Muc tieu cua file nay la giup nguoi doc co mot buc tranh day du truoc khi di vao tung class.

---

## 1. Buc tranh tong quan

Du an gom 3 module chinh:

| Module | Vai tro |
|---|---|
| `Auction-client` | JavaFX client. Hien thi UI, validate input co ban, tao request socket, nhan response/event tu server. |
| `Auction-Server` | Socket server. Nhan request, dispatch theo action, goi controller/service/repository, lam viec voi database. |
| `Auction-shared` | Hop dong chung giua client va server: DTO, action constants, socket envelope, enum/domain dung chung. |

Kien truc tong quat:

```text
JavaFX UI
  -> Client Controller
  -> Client Service
  -> SocketClient
  -> WireMessage JSON over TCP
  -> Server ClientHandler
  -> RequestDispatcher
  -> Server Controller
  -> Server Service
  -> Repository
  -> Hibernate Session / MySQL
```

Nguyen tac quan trong:

- Client khong goi truc tiep database.
- Server la noi quyet dinh nghiep vu cuoi cung.
- `Auction-shared` la noi nen dat cac DTO/action chung de client va server khong lech contract.
- Socket message la JSON 1 dong, moi message ket thuc bang newline.

---

## 2. Giao thuc socket

Client va server trao doi bang `WireMessage`.

Class chinh:

```text
Auction-shared/src/main/java/com/auction/shared/protocol/WireMessage.java
Auction-shared/src/main/java/com/auction/shared/protocol/WireMessageType.java
Auction-shared/src/main/java/com/auction/shared/protocol/ActionConstants.java
Auction-shared/src/main/java/com/auction/shared/protocol/JsonSupport.java
```

### 2.1 Client gui request

Client service goi:

```java
SocketClient.getInstance().send(action, body, responseType)
```

`SocketClient` boc thanh:

```json
{
  "type": "REQUEST",
  "requestId": "uuid",
  "action": "AUTH_LOGIN",
  "data": {
    "identity": "user01",
    "password": "123456"
  }
}
```

Y nghia field:

| Field | Y nghia |
|---|---|
| `type` | `REQUEST`, `RESPONSE`, hoac `EVENT`. |
| `requestId` | Id de client match dung response voi request dang cho. |
| `action` | Ten lenh, lay tu `ActionConstants`. |
| `data` | Payload DTO serialize sang JSON. |

### 2.2 Server tra response

Server tra lai:

```json
{
  "type": "RESPONSE",
  "requestId": "uuid",
  "action": "AUTH_LOGIN",
  "success": true,
  "message": "Login success",
  "data": {
    "user": {
      "id": 1,
      "username": "user01",
      "role": "BIDDER"
    }
  }
}
```

Client doc response trong reader thread, tim `pending[requestId]`, roi complete future cho request dang doi.

### 2.3 Server push event

He thong co thiet ke de server day event realtime:

```json
{
  "type": "EVENT",
  "action": "EVENT_BID_UPDATED",
  "data": {
    "auctionId": 10,
    "currentPrice": 1500000
  }
}
```

Client nhan event trong `SocketClient.readLoop()`, map action qua `ServerEventMapper`, roi publish vao `EventBus`.

---

## 3. Luong request tu client den server

### 3.1 Phia client

Vi du login:

```text
LoginController.handleLogin()
  -> AuthServiceImpl.login(LoginRequest)
  -> SocketClient.send(AUTH_LOGIN, request, AuthResponse.class)
  -> tao WireMessage REQUEST
  -> writer.println(json)
  -> doi RESPONSE theo requestId
```

Class lien quan:

```text
Auction-client/src/main/java/com/auction/client/feature/auth/controller/LoginController.java
Auction-client/src/main/java/com/auction/client/feature/auth/service/AuthServiceImpl.java
Auction-client/src/main/java/com/auction/client/network/SocketClient.java
```

### 3.2 Phia server

```text
ServerSocketManager.start()
  -> accept socket
  -> new ClientHandler(socket, dispatcher)
  -> ClientHandler.readLine()
  -> gson.fromJson(json, WireMessage.class)
  -> RequestDispatcher.dispatch(wireMessage)
  -> AuthController.login(bodyJson)
  -> AuthService.login(LoginRequest)
  -> UserRepository / Hibernate
  -> Response<AuthResponse>
  -> ClientHandler wrap thanh WireMessage RESPONSE
```

Class lien quan:

```text
Auction-Server/src/main/java/com/auction/server/network/ServerSocketManager.java
Auction-Server/src/main/java/com/auction/server/network/ClientHandler.java
Auction-Server/src/main/java/com/auction/server/network/RequestDispatcher.java
Auction-Server/src/main/java/com/auction/server/feature/auth/controller/AuthController.java
Auction-Server/src/main/java/com/auction/server/feature/auth/service/AuthService.java
```

---

## 4. RequestDispatcher nhan gi va dua vao controller nhu the nao?

`RequestDispatcher` nhan mot object:

```java
public Response<?> dispatch(WireMessage request)
```

No kiem tra:

- request khong null
- `type == REQUEST`
- `action` khong rong

Sau do lay:

```java
String action = request.getAction().trim();
String requestBody = request.getData() != null ? request.getData().toString() : "{}";
```

Va route:

```text
AUTH_LOGIN          -> authController.login(requestBody)
AUTH_REGISTER       -> authController.register(requestBody)
AUCTION_GET_LIST    -> auctionController.getAllAuctions(requestBody)
AUCTION_GET_DETAIL  -> auctionController.getAuctionDetail(requestBody)
BID_PLACE_BID       -> bidController.placeBid(requestBody)
SELLER_ITEM_CREATE  -> sellerController.createItem(requestBody)
...
```

Nhan xet:

- Dispatcher chi nen lam dieu huong.
- Controller moi parse JSON body thanh DTO.
- Service moi chua nghiep vu.
- Repository moi noi chuyen voi database.

Day la huong tot vi moi tang co mot trach nhiem ro rang.

---

## 5. DTO la gi va chung lien quan voi nhau nhu the nao?

DTO la "Data Transfer Object": object chi de chuyen du lieu qua bien gioi tang/module.

Trong du an nay co 3 loai DTO chinh:

### 5.1 DTO protocol

```text
WireMessage
Response<T>
```

`WireMessage` la envelope ben ngoai cua socket.

`Response<T>` la ket qua nghiep vu ben trong server/controller:

```java
Response.success("Login success", authResponse)
Response.fail("Sai ten dang nhap hoac mat khau")
```

`ClientHandler` chuyen `Response<?>` thanh `WireMessage RESPONSE`.

### 5.2 DTO request

Day la payload client gui len server.

Vi du:

| DTO | Dung cho |
|---|---|
| `LoginRequest` | Dang nhap bang username/email va password. |
| `RegisterRequest` | Dang ky tai khoan. |
| `AuctionIdRequest` | Lay detail / subscribe auction theo id. |
| `PlaceBidRequest` | Dat gia: auctionId, bidderId, amount. |
| `CreateSellerItemRequest` | Seller tao san pham/phien dau gia. |
| `UpdateSellerItemRequest` | Seller sua san pham/phien dau gia. |
| `DeleteSellerItemRequest` | Seller xoa/huy/archive san pham. |

### 5.3 DTO response/view

Day la payload server tra ve de client hien thi.

| DTO | Dung cho |
|---|---|
| `AuthResponse` | Chua `UserInfo` sau login/register. |
| `UserInfo` | Thong tin user an toan, khong co passwordHash. |
| `AuctionSummaryDto` | Row tom tat cho danh sach dau gia. |
| `AuctionDetailDto` | Chi tiet mot phien dau gia. |
| `SellerItemDto` | Row san pham trong Seller Dashboard. |
| `BidResultDto` | Ket qua dat gia neu server tra typed result. |
| `BidUpdateEventDto` | Payload event khi gia thay doi. |

### 5.4 Vi sao nen dung DTO trong `Auction-shared`?

Khong bat buoc client/server import cung mot class, nhung JSON structure phai giong nhau.

Neu client gui:

```json
{ "identity": "user01", "password": "123456" }
```

server parse vao class:

```java
record LoginRequest(String username, String password) {}
```

thi `username = null`, vi JSON khong co field `username`.

Dung DTO chung trong `Auction-shared` giup giam loi lech field.

---

## 6. Luong auth

### 6.1 Dang ky

Client:

```text
RegisterController
  -> RegisterValidator
  -> AuthServiceImpl.register(RegisterRequest)
  -> AUTH_REGISTER
```

Server:

```text
AuthController.register()
  -> parse RegisterRequest
  -> AuthService.register()
  -> validate input
  -> hash password bang PasswordUtil
  -> DbExecutor.runAndReturn()
  -> UserRepository.existsByUsername / existsByEmail
  -> save User
  -> AuthResponse.fromUserInfo()
```

Quyet dinh da dung:

- Password khong luu plain text.
- `UserInfo` khong tra `passwordHash` ve client.
- User moi mac dinh co role `BIDDER`.

### 6.2 Dang nhap

```text
LoginController
  -> AuthServiceImpl.login(LoginRequest)
  -> AUTH_LOGIN
  -> AuthController.login()
  -> AuthService.login()
  -> find user by username/email
  -> PasswordUtil.verifyPassword()
  -> AuthResponse(UserInfo)
  -> UserSession.start()
```

Luu y:

- Email duoc normalize lowercase khi login.
- Client luu user hien tai trong `UserSession`.
- Client khong luu password.

---

## 7. Luong xem danh sach va chi tiet dau gia

### 7.1 Danh sach dau gia

Client:

```text
AuctionListController.initialize()
  -> AuctionServiceImpl.fetchAuctions()
  -> AUCTION_GET_LIST
  -> AuctionDtoMapper.toSummary()
  -> TableView
```

Server:

```text
AuctionController.getAllAuctions()
  -> AuctionService.getAllAuctions(page, size)
  -> AuctionSessionRepository.findActive(page, size)
  -> map AuctionSession -> AuctionResponse
```

### 7.2 Chi tiet dau gia

```text
AuctionDetailController.setAuctionId(id)
  -> AuctionServiceImpl.fetchAuctionDetail(id)
  -> AUCTION_GET_DETAIL
  -> AuctionController.getAuctionDetail()
  -> AuctionService.getAuctionDetail(id)
  -> AuctionSessionRepository.findByIdWithDetails(id)
```

Client hien thi:

- Ten san pham
- Mo ta
- Gia khoi diem
- Gia hien tai
- Trang thai
- Seller
- Thoi gian bat dau/ket thuc

---

## 8. Luong dat gia

Client:

```text
LiveBiddingController
  -> AccessGuard.requireLogin()
  -> BidServiceImpl.placeBid(auctionId, amount)
  -> lay bidderId tu UserSession.getUserId()
  -> PlaceBidRequest(auctionId, bidderId, amount)
  -> BID_PLACE_BID
```

Server:

```text
BidController.placeBid()
  -> parse PlaceBidRequest
  -> BidService.placeBid()
  -> DbExecutor.runAndReturn()
  -> lock AuctionSession
  -> validate auction active
  -> validate amount >= currentPrice + minBidStep
  -> save Bid
  -> update currentPrice/winner/totalBids
  -> commit
```

Y tuong xu ly canh tranh:

- Dau gia co nhieu user dat gia cung luc.
- Neu chi doc current price roi update binh thuong, hai request co the cung thang sai.
- Service dung repository lock khi lay auction session, tu do dam bao moi thoi diem chi mot transaction cap nhat gia cua phien do.

Diem manh:

- Logic validate gia nam o server.
- Client chi validate toi thieu de tranh request vo ich.
- Server moi la noi quyet dinh cuoi cung.

---

## 9. Luong seller: dang san pham va xem san pham dang ban

### 9.1 Mo Seller Dashboard

Sau dieu chinh logic client:

```text
User da login -> co the vao Seller Dashboard
Admin -> them quyen vao Admin Dashboard
```

Ly do:

- He thong hien tai khong co quy trinh "nang cap thanh seller".
- Server tao user moi mac dinh `BIDDER`.
- `SellerService` khong yeu cau `role == SELLER`; no dung `sellerId` de quan ly san pham cua user.
- Vi vay neu client chan `BIDDER` vao Seller Dashboard thi chinh client tao ra rao can khong can thiet.

### 9.2 Xem san pham cua toi

Client:

```text
SellerDashboardController.loadMyItemsAsync()
  -> SellerServiceImpl.listMyItems()
  -> sellerId = UserSession.getUserId()
  -> SELLER_ITEM_LIST_MY
```

Server:

```text
SellerController.listMyItems()
  -> sellerService.listMyItems(sellerId, page, size)
  -> AuctionSessionRepository.findBySeller()
  -> map AuctionSession + AuctionItem -> SellerItemDto
```

### 9.3 Tao san pham dau gia

Client:

```text
SellerDashboardController.handleAdd()
  -> showItemFormDialog()
  -> SellerItemDto
  -> SellerServiceImpl.createItem()
  -> CreateSellerItemRequest
  -> SELLER_ITEM_CREATE
```

Server:

```text
SellerController.createItem()
  -> CreateSellerItemRequest
  -> SellerService.createItem()
  -> validate category/seller/input
  -> tao AuctionItem
  -> tao AuctionSession
  -> save trong cung transaction
```

Quan he entity:

```text
User seller
  -> AuctionItem
      -> AuctionSession
```

San pham seller tao khong chi la `AuctionItem`; no di kem `AuctionSession`, vi san pham duoc ban theo co che dau gia.

### 9.4 Sua/xoa san pham

Server co rule bao ve:

- Chi duoc sua/xoa item cua chinh seller do.
- Khong cho sua/xoa neu auction da co bid.
- Delete la soft delete/cancel/archive, khong hard delete truc tiep de giu lich su.

Day la huong dung cho he thong dau gia, vi bid/payment can lich su on dinh.

---

## 10. Vai tro user: bidder, seller, admin

Hien tai database/entity co role:

```text
BIDDER
SELLER
ADMIN
```

Nhung service hien tai van hanh theo mo hinh:

```text
User dang nhap -> co the bid
User dang nhap -> co the sell
Admin -> co them quyen admin
```

Nhan xet:

- `BIDDER` va `SELLER` khong nen la hai loai tai khoan loai tru nhau neu mot nguoi co the vua mua vua ban.
- Client da duoc dieu chinh theo huong "da login thi co the vao seller dashboard".
- Ve lau dai nen tach:

```text
role: USER / ADMIN
sellerStatus: NONE / ACTIVE / SUSPENDED
```

hoac:

```text
permissions: BID, SELL, ADMIN
```

Hien tai chua can doi database ngay, vi service dang chay duoc voi user id.

---

## 11. Transaction va database

Class nen tang:

```text
DatabaseConnection.java
HibernateUtil.java
DbExecutor.java
```

Vai tro:

| Class | Vai tro |
|---|---|
| `DatabaseConnection` | Tao HikariCP pool den MySQL. |
| `HibernateUtil` | Tao `SessionFactory`, dang ky entity, validate schema. |
| `DbExecutor` | Mo transaction, commit/rollback, gom logic transaction cho service. |

Pattern:

```java
DbExecutor.runAndReturn(() -> {
    // repository calls
    // business changes
    return result;
});
```

Diem manh:

- Service khong phai tu quan ly session/transaction.
- Repository dung `getCurrentSession()`.
- Loi thi rollback tap trung.
- Giam duplicate transaction boilerplate.

Luu y da gap:

- Voi Hibernate `current_session_context_class=thread`, mot so method session chi hop le khi transaction active.
- Vi vay trong `DbExecutor.query()`, nen begin transaction truoc khi goi cac method bi bao ve boi transaction wrapper.

---

## 12. Entity chinh va quan he

| Entity | Vai tro |
|---|---|
| `User` | Tai khoan nguoi dung/admin/seller/bidder. |
| `Category` | Danh muc san pham. |
| `AuctionItem` | Thong tin san pham: ten, mo ta, seller, category, status. |
| `AuctionSession` | Phien dau gia: gia khoi diem, gia hien tai, step, start/end time, winner, status. |
| `Bid` | Mot lan dat gia cua bidder. |
| `Payment` | Thanh toan sau khi dau gia ket thuc. |
| `ItemImage` | Anh san pham. |

Quan he cot loi:

```text
User (seller) 1 - n AuctionItem
AuctionItem 1 - 1 AuctionSession
AuctionSession 1 - n Bid
User (bidder) 1 - n Bid
AuctionSession 1 - 1 Payment
```

---

## 13. Cac quyet dinh thiet ke da dung

### 13.1 Dung socket thay vi HTTP

Ly do:

- Phu hop bai toan realtime dau gia.
- Cung mot connection co the nhan response va event.
- Co the day `EVENT_BID_UPDATED` ve client.

Diem can chu y:

- Phai co `requestId` de match response.
- Phai co reader thread rieng o client.
- Phai xu ly reconnect/mat ket noi can than.

### 13.2 Dung `WireMessage`

Truoc day client/server de lech protocol de gay loi. `WireMessage` giai quyet bang cach co envelope chung:

```text
type + requestId + action + success/message/errorCode + data
```

Diem manh:

- De them event realtime.
- Client khong bi nham response voi event.
- Debug de hon vi moi message co action/requestId.

### 13.3 Dung shared DTO

Dung `Auction-shared` cho:

- Auth request/response
- Auction DTO
- Bidding DTO
- Seller DTO
- Action constants
- Domain enum

Diem manh:

- Giam loi field lech nhau.
- Client/server cung noi mot ngon ngu.
- Refactor contract de kiem soat hon.

### 13.4 Manual dependency injection

`MainServer` la composition root:

```text
create repositories
create services
create controllers
create dispatcher
start socket server
```

Diem manh:

- Khong can framework lon.
- De thay dependency khi test.
- Tranh controller/service tu `new` lung tung repository.

---

## 14. Diem manh hien tai

1. Client-server da co contract socket ro rang bang `WireMessage`.
2. Cac action da tap trung trong `ActionConstants`.
3. Auth khong tra password hash ve client.
4. Seller workflow da co controller/service rieng, khong tron vao public auction controller.
5. Bid workflow co xu ly transaction va lock de tranh race condition.
6. `DbExecutor` tap trung transaction, giup service gon hon.
7. Client co `EventBus`, san sang cho realtime event.
8. DTO shared giup giam lech field giua client va server.
9. Client da duoc dieu chinh de mot account co the vua bid vua sell.
10. Server dung soft delete/cancel/archive cho seller item, giu lich su dau gia an toan hon.

---

## 15. Nhung van de con ton tai

### 15.1 Server dang tin `sellerId` / `bidderId` client gui len

Hien tai request:

```json
{ "sellerId": 5, "...": "..." }
{ "bidderId": 5, "...": "..." }
```

Chay duoc, nhung chua an toan. Client co the gia mao id.

Huong dung ve sau:

```text
Login thanh cong -> server tao session/token
Client gui token/requestId
Server tu lay currentUserId tu session
Request khong can sellerId/bidderId nua
```

### 15.2 Client con parse JSON tolerant

Mot so service dung `JsonElement` va mapper thu cong de chap nhan nhieu schema.

Diem tot:

- Linh hoat khi server dang thay doi.

Diem yeu:

- Contract khong chat.
- Loi schema co the bi che thanh default value.

Huong ve sau:

- Khi server da on dinh, chuyen response sang typed DTO:

```java
Response<SellerItemDto[]>
Response<AuctionSummaryDto[]>
Response<AuctionDetailDto>
```

### 15.3 Role model chua that sach

Hien co `BIDDER/SELLER/ADMIN`, nhung business mong muon mot user vua bid vua sell.

Huong ve sau:

- Doi `BIDDER/SELLER` thanh capability.
- Giu `ADMIN` la role dac biet.

### 15.4 Event realtime chua hoan thien toan bo

Client da co `EventBus` va handler event, nhung server can day event day du sau khi bid thanh cong:

```text
EVENT_BID_UPDATED
EVENT_AUCTION_STATUS_CHANGED
```

### 15.5 Schema database va entity can dong bo

Hibernate dang `hbm2ddl.auto=validate`, nghia la entity va DB phai khop.

Diem manh:

- Phat hien sai schema som.

Diem can chu y:

- Khi them/xoa field entity phai update schema/DB migration tuong ung.

---

## 16. Goi y thu tu cai thien tiep theo

Nen lam theo thu tu:

1. Hoan thien test manual cac workflow:
   - register
   - login
   - view auction list
   - view detail
   - place bid
   - create seller item
   - list seller items
   - update/delete seller item

2. Them server-side session/token:
   - khong tin `sellerId/bidderId` tu client
   - server tu lay current user

3. Chuan hoa response DTO:
   - auction list/detail tra DTO shared
   - seller list tra `List<SellerItemDto>`

4. Hoan thien realtime event:
   - sau bid thanh cong broadcast event
   - client update UI khong can refresh thu cong

5. Don role model:
   - `USER/ADMIN`
   - them `sellerStatus` neu can duyet seller

---

## 17. Tom tat ngan gon

He thong hien tai di theo kien truc:

```text
Client Controller
  -> Client Service
  -> SocketClient / WireMessage
  -> ClientHandler
  -> RequestDispatcher
  -> Controller
  -> Service
  -> Repository
  -> Hibernate/MySQL
```

Nhung y tuong quan trong:

- `WireMessage` giai quyet bai toan protocol socket.
- `ActionConstants` giai quyet bai toan action string bi lech.
- DTO shared giai quyet bai toan client/server parse sai field.
- `DbExecutor` giai quyet bai toan transaction lap lai.
- Seller workflow tao ca `AuctionItem` va `AuctionSession`, vi san pham ban ra la san pham dau gia.
- Bid workflow phai xu ly concurrency, khong chi la insert mot dong bid.
- Client hien nen coi user dang nhap la co the vua bid vua sell; admin la quyen rieng.

Trang thai hien tai: he thong da co nen tang tot de chay va test workflow dau gia, nhung con nen hoan thien auth session/token va chuan hoa typed DTO de dat muc thiet ke sach hon.
