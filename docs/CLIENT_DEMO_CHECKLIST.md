# Client Demo Checklist

Checklist này dùng để demo phần client trước khi server được migrate hoàn chỉnh.

| STT | Luồng demo | Client UI | Cần server? | Ghi chú |
|---:|---|---|---|---|
| 1 | Mở app vào Login | Done | No | `MainClient` đi qua `SceneNavigator` |
| 2 | Register form validate | Done | Yes để lưu thật | Client validate trước khi gửi |
| 3 | Login form validate | Done | Yes để đăng nhập thật | Sau login set `UserSession` |
| 4 | Điều hướng theo role | Done | Yes để nhận role thật | Bidder/Seller/Admin |
| 5 | Home role menu | Done | No nếu session mock | Ẩn/hiện Seller/Admin theo role |
| 6 | Auction list | Done | Yes | Gọi `AUCTION_GET_LIST` |
| 7 | Auction detail | Done | Yes | Gọi `AUCTION_GET_DETAIL` |
| 8 | Live bidding screen | Done | Yes | Subscribe + place bid + event |
| 9 | Bid input validate | Done | No | BigDecimal, không gửi khi sai |
| 10 | Realtime bid update | Client ready | Yes | Chờ server push `EVENT_BID_UPDATED` |
| 11 | Seller dashboard form | Done | Yes để lưu thật | Validate giá/time/name/description |
| 12 | Seller create/update/delete | Client ready | Yes | Gọi seller actions |
| 13 | Admin dashboard tối thiểu | Done | Partial | Chưa đoán API admin |
| 14 | Logout | Done | No | Clear session và quay về Login |
| 15 | Error message thống nhất | Done | Partial | Rõ hơn khi server trả lỗi đúng contract |

## Checklist trước khi chuyển sang server

- [ ] `mvn -pl Auction-client -am clean test` pass.
- [ ] Không còn DTO trùng trong `Auction-client`.
- [ ] Không hardcode action string ở controller/service.
- [ ] `UserSession` được set sau login.
- [ ] `BidServiceImpl` không gửi `bidderId = 0`.
- [ ] Seller delete không gửi `auctionId = 0` giả.
- [ ] Forgot Password không còn trong flow chính.
- [ ] Contract trong `CLIENT_SERVER_CONTRACT_LOCKED.md` được dùng làm nguồn chuẩn để migrate server.
