package com.auction.server.feature.auction.controller;

import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.dto.AuctionResponse;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.service.AuctionService;

import java.util.List;

public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController() {
        this.auctionService = new AuctionService();
    }

    public List<AuctionResponse> getAllAuctions() {
        try {
            return auctionService.getAllAuctions();
        } catch (AuctionException e) {
            throw e;
        } catch (Exception e) {
            throw new AuctionException("Unexpected error while getting auctions", e);
        }
    }

    public AuctionDetailResponse getAuctionDetail(int auctionId) {
        try {
            return auctionService.getAuctionDetail(auctionId);
        } catch (AuctionException e) {
            throw e;
        } catch (Exception e) {
            throw new AuctionException("Unexpected error while getting auction detail", e);
        }
    }

    public AuctionResponse createAuction(CreateAuctionRequest request) {
        try {
            return auctionService.createAuction(request);
        } catch (AuctionException e) {
            throw e;
        } catch (Exception e) {
            throw new AuctionException("Unexpected error while creating auction", e);
        }
    }
}