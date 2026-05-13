package com.auction.server.feature.auction.service;

import com.auction.server.exception.DataAccessException;
import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.dto.AuctionResponse;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.repository.AuctionItemRepository;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.auction.repository.CategoryRepository;

import java.math.BigDecimal;
import java.util.List;

public class AuctionService {

    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final CategoryRepository categoryRepository;

    public AuctionService() {
        this.auctionSessionRepository = new AuctionSessionRepository();
        this.auctionItemRepository = new AuctionItemRepository();
        this.categoryRepository = new CategoryRepository();
    }

    public List<AuctionResponse> getAllAuctions() {
        try {
            return auctionSessionRepository.findAll();
        } catch (DataAccessException e) {
            throw new AuctionException("System error while getting auctions", e);
        }
    }

    public AuctionDetailResponse getAuctionDetail(int auctionId) {
        if (auctionId <= 0) {
            throw new AuctionException("Invalid auction id");
        }

        try {
            AuctionDetailResponse response = auctionSessionRepository.findDetailById(auctionId);

            if (response == null) {
                throw new AuctionException("Auction not found");
            }

            return response;

        } catch (DataAccessException e) {
            throw new AuctionException("System error while getting auction detail", e);
        }
    }

    public AuctionResponse createAuction(CreateAuctionRequest request) {
        validateCreateAuction(request);

        try {
            if (!categoryRepository.existsById(request.getCategoryId())) {
                throw new AuctionException("Category not found");
            }

            int itemId = auctionItemRepository.save(
                    request.getSellerId(),
                    request.getCategoryId(),
                    request.getItemName().trim(),
                    request.getDescription().trim()
            );

            int auctionId = auctionSessionRepository.save(
                    itemId,
                    request.getStartingPrice(),
                    request.getStartTime(),
                    request.getEndTime()
            );

            AuctionDetailResponse detail = auctionSessionRepository.findDetailById(auctionId);

            return new AuctionResponse(
                    detail.getAuctionId(),
                    detail.getItemId(),
                    detail.getItemName(),
                    detail.getStartingPrice(),
                    detail.getCurrentPrice(),
                    detail.getStartTime(),
                    detail.getEndTime(),
                    detail.getStatus()
            );

        } catch (DataAccessException e) {
            throw new AuctionException("System error while creating auction", e);
        }
    }

    private void validateCreateAuction(CreateAuctionRequest request) {
        if (request == null) {
            throw new AuctionException("Create auction request is required");
        }

        if (request.getSellerId() <= 0) {
            throw new AuctionException("Invalid seller id");
        }

        if (request.getCategoryId() <= 0) {
            throw new AuctionException("Invalid category id");
        }

        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new AuctionException("Item name is required");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new AuctionException("Description is required");
        }

        if (request.getStartingPrice() == null ||
                request.getStartingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AuctionException("Starting price must be greater than 0");
        }

        if (request.getStartTime() == null) {
            throw new AuctionException("Start time is required");
        }

        if (request.getEndTime() == null) {
            throw new AuctionException("End time is required");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new AuctionException("End time must be after start time");
        }
    }
}