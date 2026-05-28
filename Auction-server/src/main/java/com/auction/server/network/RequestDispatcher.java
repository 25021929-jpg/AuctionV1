package com.auction.server.network;

import com.auction.server.feature.auth.controller.AuthController;
import com.auction.server.feature.auction.controller.AuctionController;
import com.auction.server.feature.bidding.controller.BidController;
import com.auction.server.feature.seller.controller.SellerController;
import com.auction.shared.dto.Response;
import com.auction.shared.protocol.ActionConstants;
import com.auction.shared.protocol.WireMessage;
import com.auction.shared.protocol.WireMessageType;

public class RequestDispatcher {

    private final AuthController authController;
    private final AuctionController auctionController;
    private final BidController bidController;
    private final SellerController sellerController;

    public RequestDispatcher(AuthController authController,
                             AuctionController auctionController,
                             BidController bidController,
                             SellerController sellerController) {
        this.authController = authController;
        this.auctionController = auctionController;
        this.bidController = bidController;
        this.sellerController = sellerController;
    }

    /**
     * Routes a socket envelope to the matching feature controller.
     *
     * <p>The network layer owns {@link WireMessage}. Controllers still receive the
     * raw JSON body so they can be migrated independently in smaller steps.</p>
     */
    public Response<?> dispatch(WireMessage request) {
        if (request == null) {
            return Response.fail("Request is null");
        }

        if (request.getType() != WireMessageType.REQUEST) {
            return Response.fail("Message type must be REQUEST");
        }

        if (request.getAction() == null || request.getAction().trim().isEmpty()) {
            return Response.fail("Action is required");
        }

        String action = request.getAction().trim();
        String requestBody = request.getData() != null ? request.getData().toString() : "{}";

        try {
            switch (action) {
                case ActionConstants.AUTH_REGISTER:
                    return authController.register(requestBody);

                case ActionConstants.AUTH_LOGIN:
                    return authController.login(requestBody);

                case "AUCTION_CREATE":
                    return auctionController.createAuction(requestBody);

                case ActionConstants.AUCTION_GET_LIST:
                    return auctionController.getAllAuctions(requestBody);

                case ActionConstants.AUCTION_GET_DETAIL:
                    return auctionController.getAuctionDetail(requestBody);

                case ActionConstants.AUCTION_SUBSCRIBE:
                case ActionConstants.AUCTION_UNSUBSCRIBE:
                    return Response.success("Subscription updated", null);

                case ActionConstants.BID_PLACE_BID:
                    return bidController.placeBid(requestBody);

                case "BID_HISTORY":
                    return bidController.getBidHistory(requestBody);

                case ActionConstants.SELLER_ITEM_LIST_MY:
                    return sellerController.listMyItems(requestBody);

                case ActionConstants.SELLER_ITEM_CREATE:
                    return sellerController.createItem(requestBody);

                case ActionConstants.SELLER_ITEM_UPDATE:
                    return sellerController.updateItem(requestBody);

                case ActionConstants.SELLER_ITEM_DELETE:
                    return sellerController.deleteItem(requestBody);

                default:
                    return Response.fail("Unsupported action: " + action);
            }
        } catch (Exception e) {
            return Response.fail("Error processing request: " + e.getMessage());
        }
    }
}
