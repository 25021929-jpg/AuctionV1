package com.auction.server.network;

import com.auction.server.feature.auth.controller.AuthController;
import com.auction.server.feature.auction.controller.AuctionController;
import com.auction.server.feature.bidding.controller.BidController;
import com.auction.shared.dto.Response;
import com.auction.shared.protocol.ActionConstants; // Sử dụng Single Source of Truth từ shared protocol
import com.auction.shared.protocol.WireMessage;      // Thay thế class Request cũ bằng WireMessage

public class RequestDispatcher {

    private final AuthController authController;
    private final AuctionController auctionController;
    private final BidController bidController;

    // Toàn bộ các Controller này chỉ được khởi tạo DUY NHẤT 1 LẦN khi Server bật lên
    // Áp dụng DI: Nhận toàn bộ các Controller đã dựng sẵn dưới dạng Singleton từ MainServer đẩy sang
    public RequestDispatcher(AuthController authController,
                             AuctionController auctionController,
                             BidController bidController) {
        this.authController = authController;
        this.auctionController = auctionController;
        this.bidController = bidController;
    }

    /**
     * Nhận gói tin Envelope mạng WireMessage, bóc tách và phân phối đến đúng Controller.
     */
    public Response<?> dispatch(WireMessage request) {

        if (request == null) {
            return Response.fail("Request is null");
        }

        if (request.getAction() == null || request.getAction().trim().isEmpty()) {
            return Response.fail("Action is required");
        }

        String action = request.getAction().trim();

        // Chuyển đổi dữ liệu nghiệp vụ (data) bên trong WireMessage thành dạng String JSON thô
        // để truyền vào cấu trúc các hàm có sẵn của Controller (ví dụ: login(String requestBody)).
        String requestBody = request.getData() != null ? request.getData().toString() : "";

        try {
            switch (action) {

                // ===== AUTH ACTIONS (Khớp đồng bộ với ActionConstants) =====
                case ActionConstants.AUTH_REGISTER:
                    return authController.register(requestBody);

                case ActionConstants.AUTH_LOGIN:
                    return authController.login(requestBody);

                // ===== AUCTION ACTIONS =====
                case "AUCTION_CREATE":
                    return auctionController.createAuction(requestBody);

                case ActionConstants.AUCTION_GET_LIST: // Thay vì chuỗi "AUCTION_LIST" cũ để khớp Client
                    return auctionController.getAllAuctions(requestBody);

                case ActionConstants.AUCTION_GET_DETAIL: // Thay vì "AUCTION_DETAIL" cũ
                    return auctionController.getAuctionDetail(requestBody);

                // ===== BIDDING ACTIONS =====
                case ActionConstants.BID_PLACE_BID: // Khớp hoàn toàn với hằng số "BID_PLACE_BID"
                    return bidController.placeBid(requestBody);

                case "BID_HISTORY":
                    return bidController.getBidHistory(requestBody);

                default:
                    return Response.fail("Unsupported action: " + action);
            }
        } catch (Exception e) {
            return Response.fail("Error processing request: " + e.getMessage());
        }
    }
}