package com.auction.server.feature.auction.controller;

import com.auction.server.database.HibernateUtil;
import com.auction.server.entity.AuctionSession;
import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.service.AuctionService;
import com.auction.server.feature.auction.repository.HibernateAuctionItemRepository;
import com.auction.server.feature.auction.repository.HibernateAuctionSessionRepository;
import com.auction.server.feature.auction.repository.HibernateCategoryRepository;
import com.shared.dto.Request;
import com.shared.dto.Response;

import java.util.List;

public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController() {
        this.auctionService = new AuctionService(
            new HibernateAuctionSessionRepository(HibernateUtil.getSessionFactory()),
            new HibernateAuctionItemRepository(HibernateUtil.getSessionFactory()),
            new HibernateCategoryRepository(HibernateUtil.getSessionFactory())
        );
    }

    // Constructor với DI (cho test)
    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public Response<List<AuctionSession>> getAllAuctions(String requestBody) {
        try {
            // Parse pagination params từ request
            int page = 0;
            int size = 20;
            List<AuctionSession> auctions = auctionService.getAllAuctions(page, size);
            return Response.success(auctions);
        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Unexpected error while getting auctions: " + e.getMessage());
        }
    }

    public Response<AuctionSession> getAuctionDetail(String requestBody) {
        try {
            // Parse auctionId từ request
            long auctionId = Long.parseLong(requestBody);
            AuctionSession auction = auctionService.getAuctionDetail(auctionId);
            return Response.success(auction);
        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Unexpected error while getting auction detail: " + e.getMessage());
        }
    }

    public Response<AuctionSession> createAuction(String requestBody) {
        try {
            // Parse CreateAuctionRequest từ JSON requestBody
            // (giả định có ObjectMapper hoặc JsonParser)
            CreateAuctionRequest request = parseCreateAuctionRequest(requestBody);
            AuctionSession auction = auctionService.createAuction(request);
            return Response.success(auction);
        } catch (AuctionException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Unexpected error while creating auction: " + e.getMessage());
        }
    }

    /**
     * Helper method để parse CreateAuctionRequest từ JSON string.
     * (Trong thực tế dùng Jackson hoặc Gson)
     */
    private CreateAuctionRequest parseCreateAuctionRequest(String json) {
        // TODO: Implement JSON parsing (dùng Jackson/Gson)
        throw new UnsupportedOperationException("JSON parsing not implemented yet");
    }
}