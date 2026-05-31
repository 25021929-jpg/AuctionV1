package com.auction.server.network;

import com.auction.server.feature.auction.controller.AuctionController;
import com.auction.server.feature.auth.controller.AuthController;
import com.auction.server.feature.bidding.controller.BidController;
import com.auction.server.feature.seller.controller.SellerController;
import com.auction.server.feature.wallet.controller.WalletController;
import com.auction.shared.dto.Response;
import com.auction.shared.protocol.ActionConstants;
import com.auction.shared.protocol.WireMessage;
import com.auction.shared.protocol.WireMessageType;
import com.google.gson.JsonObject;

public class RequestDispatcher {

  private final AuthController authController;
  private final AuctionController auctionController;
  private final BidController bidController;
  private final SellerController sellerController;
  private final WalletController walletController;

  public RequestDispatcher(
      AuthController authController,
      AuctionController auctionController,
      BidController bidController,
      SellerController sellerController,
      WalletController walletController) {
    this.authController = authController;
    this.auctionController = auctionController;
    this.bidController = bidController;
    this.sellerController = sellerController;
    this.walletController = walletController;
  }

  public Response<?> dispatch(WireMessage request) {
    return dispatch(request, null);
  }

  public Response<?> dispatch(WireMessage request, ClientHandler clientHandler) {
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

        case ActionConstants.CATEGORY_GET_LIST:
          return auctionController.getCategories(requestBody);

        case ActionConstants.AUCTION_SUBSCRIBE:
          return subscribeAuction(request, clientHandler);

        case ActionConstants.AUCTION_UNSUBSCRIBE:
          return unsubscribeAuction(request, clientHandler);

        case ActionConstants.BID_PLACE_BID:
          return bidController.placeBid(requestBody);

        case ActionConstants.BID_GET_HISTORY:
          return bidController.getBidHistory(requestBody);

        case ActionConstants.WALLET_GET_SUMMARY:
          return walletController.getSummary(requestBody);

        case ActionConstants.WALLET_DEPOSIT:
          return walletController.deposit(requestBody);

        case ActionConstants.WALLET_GET_TRANSACTIONS:
          return walletController.getTransactions(requestBody);

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

  private Response<Void> subscribeAuction(WireMessage request, ClientHandler clientHandler) {
    if (clientHandler == null) {
      return Response.fail("Client handler is required for subscription");
    }

    Long auctionId = extractAuctionId(request);
    if (auctionId == null || auctionId <= 0) {
      return Response.fail("AuctionId is required for subscription");
    }

    AuctionRoomRegistry.join(auctionId, clientHandler);
    return Response.success("Subscription updated", null);
  }

  private Response<Void> unsubscribeAuction(WireMessage request, ClientHandler clientHandler) {
    if (clientHandler == null) {
      return Response.fail("Client handler is required for subscription");
    }

    Long auctionId = extractAuctionId(request);
    if (auctionId == null || auctionId <= 0) {
      return Response.fail("AuctionId is required for subscription");
    }

    AuctionRoomRegistry.leave(auctionId, clientHandler);
    return Response.success("Subscription updated", null);
  }

  private Long extractAuctionId(WireMessage request) {
    if (request == null || request.getData() == null || !request.getData().isJsonObject()) {
      return null;
    }

    JsonObject data = request.getData().getAsJsonObject();
    if (data.has("auctionId") && !data.get("auctionId").isJsonNull()) {
      return data.get("auctionId").getAsLong();
    }
    if (data.has("auctionSessionId") && !data.get("auctionSessionId").isJsonNull()) {
      return data.get("auctionSessionId").getAsLong();
    }
    return null;
  }
}
