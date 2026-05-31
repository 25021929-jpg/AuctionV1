package com.auction.server.feature.bidding.controller;

import com.auction.server.feature.bidding.BidException;
import com.auction.server.feature.bidding.dto.BidResponse;
import com.auction.server.feature.bidding.dto.PlaceBidRequest;
import com.auction.server.feature.bidding.service.BidService;
import com.auction.server.network.BroadcastService;
import com.auction.shared.dto.Response;
import com.auction.shared.protocol.ActionConstants;
import com.auction.shared.protocol.JsonSupport;
import com.auction.shared.protocol.WireMessage;
import com.auction.shared.protocol.WireMessageType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.util.List;

public class BidController {

    private final BidService bidService;
    private final Gson gson = JsonSupport.createGson();

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    public Response<BidResponse> placeBid(String requestBody) {
        try {
            PlaceBidRequest request = parsePlaceBidRequest(requestBody);
            BidResponse response = bidService.placeBid(request);
            BroadcastService.broadcastToRoom(response.getAuctionSessionId(), toBidUpdatedEvent(response));
            return Response.success("Bid placed successfully", response);
        } catch (BidException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Unexpected error while placing bid: " + e.getMessage());
        }
    }

    public Response<List<BidResponse>> getBidHistory(String requestBody) {
        try {
            JsonObject obj = gson.fromJson(requestBody, JsonObject.class);
            Long auctionId = getLong(obj, "auctionId");
            if (auctionId == null) {
                auctionId = getLong(obj, "auctionSessionId");
            }
            int limit = obj != null && obj.has("limit") ? obj.get("limit").getAsInt() : 10;

            List<BidResponse> bidHistory = bidService.getBidHistory(auctionId, limit);
            return Response.success("Bid history loaded", bidHistory);
        } catch (BidException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Unexpected error while getting bid history: " + e.getMessage());
        }
    }

    private PlaceBidRequest parsePlaceBidRequest(String json) {
        JsonObject obj = gson.fromJson(json, JsonObject.class);
        if (obj == null) {
            throw new BidException("Request đặt giá không hợp lệ");
        }

        Long auctionSessionId = getLong(obj, "auctionSessionId");
        if (auctionSessionId == null) {
            auctionSessionId = getLong(obj, "auctionId");
        }

        Long bidderId = getLong(obj, "bidderId");

        BigDecimal bidAmount = getBigDecimal(obj, "bidAmount");
        if (bidAmount == null) {
            bidAmount = getBigDecimal(obj, "amount");
        }

        return new PlaceBidRequest(auctionSessionId, bidderId, bidAmount);
    }

    private Long getLong(JsonObject obj, String field) {
        if (obj == null || !obj.has(field) || obj.get(field).isJsonNull()) {
            return null;
        }
        return obj.get(field).getAsLong();
    }

    private BigDecimal getBigDecimal(JsonObject obj, String field) {
        if (obj == null || !obj.has(field) || obj.get(field).isJsonNull()) {
            return null;
        }
        return obj.get(field).getAsBigDecimal();
    }

    private WireMessage toBidUpdatedEvent(BidResponse response) {
        JsonObject data = new JsonObject();
        data.addProperty("auctionId", response.getAuctionSessionId());
        data.addProperty("auctionSessionId", response.getAuctionSessionId());
        data.addProperty("bidId", response.getBidId());
        data.addProperty("bidderId", response.getBidderId());
        data.addProperty("bidderUsername", response.getBidderUsername());
        data.add("bidAmount", gson.toJsonTree(response.getBidAmount()));
        data.add("currentPrice", gson.toJsonTree(response.getBidAmount()));
        if (response.getBidTime() != null) {
            data.addProperty("bidTime", response.getBidTime().toString());
        }
        data.addProperty("timestamp", System.currentTimeMillis());
        data.add("bid", gson.toJsonTree(response));

        WireMessage event = new WireMessage();
        event.setType(WireMessageType.EVENT);
        event.setAction(ActionConstants.EVENT_BID_UPDATED);
        event.setData(data);
        return event;
    }
}
