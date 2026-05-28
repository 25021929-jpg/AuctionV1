package com.auction.server.network;

import com.auction.server.feature.auth.controller.AuthController;
import com.auction.server.feature.auction.controller.AuctionController;
import com.auction.server.feature.bidding.controller.BidController;
import com.auction.shared.dto.Request;
import com.auction.shared.dto.Response;

public class RequestDispatcher {

    private final AuthController authController;
    private final AuctionController auctionController;
    private final BidController bidController;

    public RequestDispatcher() {
        this.authController = new AuthController();
        this.auctionController = new AuctionController();
        this.bidController = new BidController();
    }

    public Response<?> dispatch(Request request) {

        // Kiểm tra request có null không
        if (request == null) {
            return Response.fail("Request is null");
        }

        // Kiểm tra action có null/rỗng không
        if (request.getAction() == null || request.getAction().trim().isEmpty()) {
            return Response.fail("Action is required");
        }

        String action = request.getAction().trim();

        // Dựa vào action để gọi đúng controller
        try {
            switch (action) {

                // ===== AUTH ACTIONS =====
                case "AUTH_REGISTER":
                    return authController.register(request.getBody());

                case "AUTH_LOGIN":
                    return authController.login(request.getBody());

                // ===== AUCTION ACTIONS =====
                case "AUCTION_CREATE":
                    return auctionController.createAuction(request.getBody());

                case "AUCTION_LIST":
                    return auctionController.getAllAuctions(request.getBody());

                case "AUCTION_DETAIL":
                    return auctionController.getAuctionDetail(request.getBody());

                // ===== BIDDING ACTIONS =====
                case "BID_PLACE":
                    return bidController.placeBid(request.getBody());

                case "BID_HISTORY":
                    return bidController.getBidHistory(request.getBody());

                default:
                    return Response.fail("Unsupported action: " + action);
            }
        } catch (Exception e) {
            return Response.fail("Error processing request: " + e.getMessage());
        }
    }
}