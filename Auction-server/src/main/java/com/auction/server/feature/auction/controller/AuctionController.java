package com.auction.server.feature.auction.controller;

import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.dto.AuctionResponse;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.service.AuctionService;
import com.auction.shared.dto.Response;
import com.google.gson.Gson;

import java.util.List;

public class AuctionController {

    private final AuctionService auctionService;
    private final Gson gson;

    public AuctionController() {
        this.auctionService = new AuctionService();
        this.gson = new Gson();
    }

    // Lấy danh sách tất cả phiên đấu giá
    public Response<List<AuctionResponse>> getAllAuctions() {
        try {
            List<AuctionResponse> result = auctionService.getAllAuctions();
            return Response.success("Get auctions success", result);

        } catch (AuctionException e) {
            return Response.fail(e.getMessage());

        } catch (Exception e) {
            return Response.fail("Internal server error");
        }
    }

    // Lấy chi tiết một phiên đấu giá
    public Response<AuctionDetailResponse> getAuctionDetail(String body) {
        try {
            // body gửi lên là {"auctionId": 5}
            CreateAuctionRequest temp = gson.fromJson(body, CreateAuctionRequest.class);
            // dùng record đơn giản hơn — giải thích bên dưới
            int auctionId = gson.fromJson(body, AuctionIdRequest.class).auctionId();

            AuctionDetailResponse result = auctionService.getAuctionDetail(auctionId);
            return Response.success("Get auction detail success", result);

        } catch (AuctionException e) {
            return Response.fail(e.getMessage());

        } catch (Exception e) {
            return Response.fail("Internal server error");
        }
    }

    // Tạo phiên đấu giá mới
    public Response<AuctionResponse> createAuction(String body) {
        try {
            CreateAuctionRequest request = gson.fromJson(body, CreateAuctionRequest.class);

            AuctionResponse result = auctionService.createAuction(request);
            return Response.success("Create auction success", result);

        } catch (AuctionException e) {
            return Response.fail(e.getMessage());

        } catch (Exception e) {
            return Response.fail("Internal server error");
        }
    }

    // Record nhỏ để parse auctionId từ body
    private record AuctionIdRequest(int auctionId) {}
}