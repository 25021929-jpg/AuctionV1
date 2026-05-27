package com.auction.server.feature.auction.service;

import com.auction.server.database.DbExecutor;
import com.auction.server.database.HibernateUtil;
import com.auction.server.entity.AuctionItem;
import com.auction.server.entity.AuctionSession;
import com.auction.server.entity.User;
import com.auction.server.exception.DataAccessException;
import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.repository.AuctionItemRepository;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.auction.repository.CategoryRepository;
import com.auction.server.feature.auction.repository.HibernateAuctionItemRepository;
import com.auction.server.feature.auction.repository.HibernateAuctionSessionRepository;
import com.auction.server.feature.auction.repository.HibernateCategoryRepository;

import java.math.BigDecimal;
import java.util.List;

public class AuctionService {

    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final CategoryRepository categoryRepository;

    // Constructor với Dependency Injection
    public AuctionService(
            AuctionSessionRepository auctionSessionRepository,
            AuctionItemRepository auctionItemRepository,
            CategoryRepository categoryRepository
    ) {
        this.auctionSessionRepository = auctionSessionRepository;
        this.auctionItemRepository = auctionItemRepository;
        this.categoryRepository = categoryRepository;
    }

    // Constructor mặc định với singleton initialization
    public AuctionService() {
        this(
            new HibernateAuctionSessionRepository(HibernateUtil.getSessionFactory()),
            new HibernateAuctionItemRepository(HibernateUtil.getSessionFactory()),
            new HibernateCategoryRepository(HibernateUtil.getSessionFactory())
        );
    }

    public List<AuctionSession> getAllAuctions(int page, int size) {
        try {
            return DbExecutor.query(() ->
                auctionSessionRepository.findActive(page, size)
            );
        } catch (DataAccessException e) {
            throw new AuctionException("System error while getting auctions", e);
        }
    }

    public AuctionSession getAuctionDetail(Long auctionId) {
        if (auctionId == null || auctionId <= 0) {
            throw new AuctionException("Invalid auction id");
        }

        try {
            return DbExecutor.query(() -> {
                var result = auctionSessionRepository.findByIdWithDetails(auctionId);
                if (result.isEmpty()) {
                    throw new AuctionException("Auction not found");
                }
                return result.get();
            });
        } catch (AuctionException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new AuctionException("System error while getting auction detail", e);
        }
    }

    public AuctionSession createAuction(CreateAuctionRequest request) {
        validateCreateAuction(request);

        try {
            return DbExecutor.runAndReturn(() -> {
                // Validate category exists
                if (!categoryRepository.findById(request.getCategoryId()).isPresent()) {
                    throw new AuctionException("Category not found");
                }

                // Create AuctionItem entity (type-safe, ORM managed)
                User seller = new User();
                seller.setId((long) request.getSellerId());

                AuctionItem item = AuctionItem.builder()
                        .seller(seller) // Proxy reference
                        .category(categoryRepository.getReference(request.getCategoryId()))
                        .itemName(request.getItemName().trim())
                        .description(request.getDescription().trim())
                        .status(AuctionItem.ItemStatus.DRAFT)
                        .condition(AuctionItem.ItemCondition.GOOD)
                        .build();

                item = auctionItemRepository.save(item);

                // Create AuctionSession entity
                AuctionSession auction = AuctionSession.builder()
                        .item(item)
                        .startingPrice(request.getStartingPrice())
                        .currentPrice(request.getStartingPrice())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .status(AuctionSession.AuctionStatus.SCHEDULED)
                        .build();

                return auctionSessionRepository.save(auction);
            });

        } catch (AuctionException e) {
            throw e;
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