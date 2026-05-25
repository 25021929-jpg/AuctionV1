package com.auction.server.network;

import com.auction.server.feature.auth.controller.AuthController;
import com.auction.server.feature.auction.controller.AuctionController; // ← thêm
import com.auction.shared.dto.Request;
import com.auction.shared.dto.Response;

public class RequestDispatcher {

    private final AuthController authController;
    private final AuctionController auctionController;

    public RequestDispatcher() {
        this.authController = new AuthController();
        this.auctionController = new AuctionController();
    }

    public Response<?> dispatch(Request request) {
        if (request == null) return Response.fail("Request is null");
        if (request.getAction() == null || request.getAction().trim().isEmpty())
            return Response.fail("Action is required");

        String action = request.getAction().trim();

        switch (action) {
            // --- Auth ---
            case "AUTH_REGISTER":       return authController.register(request.getBody());
            case "AUTH_LOGIN":          return authController.login(request.getBody());
            case "AUTH_FORGOT_PASSWORD":return authController.forgotPassword(request.getBody());
            case "AUTH_RESET_PASSWORD": return authController.resetPassword(request.getBody());

            // --- Auction --- 
            case "AUCTION_GET_ALL":     return auctionController.getAllAuctions();
            case "AUCTION_GET_DETAIL":  return auctionController.getAuctionDetail(request.getBody());
            case "AUCTION_CREATE":      return auctionController.createAuction(request.getBody());

            default: return Response.fail("Unsupported action: " + action);
        }
    }
}