package com.auction.server.feature.bidding.controller;

import com.auction.server.database.HibernateUtil;
import com.auction.server.feature.auction.repository.HibernateAuctionSessionRepository;
import com.auction.server.feature.bidding.BidException;
import com.auction.server.feature.bidding.dto.BidResponse;
import com.auction.server.feature.bidding.dto.PlaceBidRequest;
import com.auction.server.feature.bidding.repository.*;
import com.auction.server.feature.bidding.service.BidService;
import com.auction.shared.dto.Response;

import java.util.List;

public class BidController {

    private final BidService bidService;

    // Constructor với DI (cho test)
    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    /**
     * Đặt giá cho phiên đấu giá.
     * Request body: {"auctionId": 123, "bidderId": 456, "bidAmount": 5000000}
     */
    public Response<BidResponse> placeBid(String requestBody) {
        try {
            // Parse PlaceBidRequest từ JSON requestBody
            PlaceBidRequest request = parsePlaceBidRequest(requestBody);
            BidResponse response = bidService.placeBid(request);
            return Response.success(response);
        } catch (BidException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Unexpected error while placing bid: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử đặt giá của một phiên.
     * Request body: {"auctionId": 123, "limit": 10}
     */
    public Response<List<BidResponse>> getBidHistory(String requestBody) {
        try {
            // Parse auctionId và limit từ request
            long auctionId = Long.parseLong(requestBody); // Simple parse
            // TODO: Implement proper JSON parsing
            List<BidResponse> bidHistory = bidService.getBidHistory(auctionId, 10);
            return Response.success(bidHistory);
        } catch (BidException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Unexpected error while getting bid history: " + e.getMessage());
        }
    }

    /**
     * Helper method để parse PlaceBidRequest từ JSON string.
     * (Trong thực tế dùng Jackson hoặc Gson)
     */
    private PlaceBidRequest parsePlaceBidRequest(String json) {
        // TODO: Implement JSON parsing (dùng Jackson/Gson)
        throw new UnsupportedOperationException("JSON parsing not implemented yet");
    }
}
