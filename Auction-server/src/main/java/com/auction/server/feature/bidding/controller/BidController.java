package com.auction.server.feature.bidding.controller;

import com.auction.server.feature.bidding.BidException;
import com.auction.server.feature.bidding.dto.BidResponse;
import com.auction.server.feature.bidding.dto.PlaceBidRequest;
import com.auction.server.feature.bidding.service.BidService;
import com.auction.shared.dto.Response;
import com.google.gson.Gson;

import java.util.List;

/**
 * Controller nhận request từ RequestDispatcher, gọi BidService, trả Response.
 *
 * Pattern y hệt AuctionController:
 *   - Nhận String body (JSON từ socket)
 *   - Gson parse → DTO
 *   - Gọi service
 *   - Trả Response.success() hoặc Response.fail()
 */
public class BidController {

    private final BidService bidService;
    private final Gson gson;

    public BidController() {
        this.bidService = new BidService();
        this.gson = new Gson();
    }

    // ===================================================================
    // ĐẶT GIÁ
    // ===================================================================

    /**
     * body JSON:
     * {
     *   "auctionSessionId": 1,
     *   "bidderId": 5,
     *   "bidAmount": 1500000
     * }
     */
    public Response<BidResponse> placeBid(String body) {
        try {
            PlaceBidRequest request = gson.fromJson(body, PlaceBidRequest.class);
            BidResponse result = bidService.placeBid(request);
            return Response.success("Đặt giá thành công", result);

        } catch (BidException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Internal server error: " + e.getMessage());
        }
    }

    // ===================================================================
    // LẤY DANH SÁCH BID CỦA MỘT PHIÊN
    // ===================================================================

    /**
     * body JSON: {"auctionSessionId": 1}
     */
    public Response<List<BidResponse>> getBidsByAuction(String body) {
        try {
            int auctionSessionId = gson.fromJson(body, AuctionSessionIdRequest.class).auctionSessionId();
            List<BidResponse> result = bidService.getBidsByAuction(auctionSessionId);
            return Response.success("Lấy danh sách bid thành công", result);

        } catch (BidException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Internal server error: " + e.getMessage());
        }
    }

    // Gson dùng cái này để parse JSON {"auctionSessionId": 1} → object Java
    private record AuctionSessionIdRequest(int auctionSessionId) {}
}