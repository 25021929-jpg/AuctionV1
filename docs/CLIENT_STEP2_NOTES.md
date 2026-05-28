# CLIENT_STEP2_NOTES

## Mục tiêu step 2

Hoàn thiện tiếp phần Auction detail + Seller dashboard theo đúng thứ tự ưu tiên client:

1. Dọn lifecycle EventBus cho các controller còn thiếu.
2. Dùng `BigDecimal` nhất quán hơn ở Seller form.
3. Validate Seller CRUD trước khi gửi request.
4. Tránh gửi dữ liệu giả như `auctionId = 0`.
5. Bổ sung test nhỏ cho logic không phụ thuộc JavaFX/server.

## File chính đã sửa

- `Auction-client/src/main/java/com/auction/client/core/util/MoneyFormat.java`
- `Auction-client/src/main/java/com/auction/client/feature/seller/validator/SellerItemFormValidator.java`
- `Auction-client/src/main/java/com/auction/client/feature/seller/controller/SellerDashboardController.java`
- `Auction-client/src/main/java/com/auction/client/feature/seller/service/SellerService.java`
- `Auction-client/src/main/java/com/auction/client/feature/seller/service/SellerServiceImpl.java`
- `Auction-client/src/main/java/com/auction/client/feature/seller/mapper/SellerItemDtoMapper.java`
- `Auction-client/src/main/java/com/auction/client/feature/auction/controller/AuctionListController.java`
- `Auction-client/src/main/java/com/auction/client/feature/auction/controller/AuctionDetailController.java`
- `Auction-client/src/main/resources/com/auction/client/feature/auction/view/auction-detail-view.fxml`
- `Auction-shared/src/main/java/com/auction/shared/dto/seller/SellerItemDto.java`
- `Auction-shared/src/main/java/com/auction/shared/dto/seller/DeleteSellerItemRequest.java`

## Việc tiếp theo sau khi build pass

1. Nối Forgot Password vào `AuthService` hoặc ẩn khỏi demo nếu chưa làm server.
2. Bổ sung test cho `AuthServiceImpl` bằng mock/fake `ServerCommunicator`.
3. Bổ sung test cho `BidServiceImpl` và `UserSession`.
4. Sau đó mới chuyển sang chuẩn hóa server theo contract.
