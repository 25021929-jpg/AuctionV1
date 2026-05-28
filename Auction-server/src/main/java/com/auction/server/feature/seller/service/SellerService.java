package com.auction.server.feature.seller.service;

import com.auction.server.database.DbExecutor;
import com.auction.server.entity.AuctionItem;
import com.auction.server.entity.AuctionSession;
import com.auction.server.entity.Category;
import com.auction.server.entity.User;
import com.auction.server.feature.auction.repository.AuctionItemRepository;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.auction.repository.CategoryRepository;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.seller.SellerException;
import com.auction.shared.dto.seller.CreateSellerItemRequest;
import com.auction.shared.dto.seller.DeleteSellerItemRequest;
import com.auction.shared.dto.seller.SellerItemDto;
import com.auction.shared.dto.seller.UpdateSellerItemRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SellerService owns seller-facing item management.
 *
 * <p>Although seller screens talk about "items", an item being sold in this app is
 * represented by two database concepts: {@link AuctionItem} for product metadata and
 * {@link AuctionSession} for price/time/status. Keeping that orchestration here avoids
 * pushing seller-specific rules into the public AuctionController.</p>
 */
public class SellerService {

    private static final BigDecimal DEFAULT_MIN_BID_STEP = BigDecimal.valueOf(1000);

    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public SellerService(AuctionSessionRepository auctionSessionRepository,
                         AuctionItemRepository auctionItemRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository) {
        this.auctionSessionRepository = auctionSessionRepository;
        this.auctionItemRepository = auctionItemRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<SellerItemDto> listMyItems(long sellerId, int page, int size) {
        validateSellerId(sellerId);
        validatePage(page, size);

        return DbExecutor.query(() ->
                auctionSessionRepository.findBySeller(sellerId, page, size)
                        .stream()
                        .map(this::toDto)
                        .toList()
        );
    }

    public SellerItemDto createItem(CreateSellerItemRequest request) {
        validateCreate(request);

        return DbExecutor.runAndReturn(() -> {
            categoryRepository.findById(toCategoryId(request.getCategoryId()))
                    .orElseThrow(() -> new SellerException("Category not found: " + request.getCategoryId()));

            // getReference avoids loading full User/Category rows when we only need foreign keys.
            User seller = userRepository.getReference(request.getSellerId());
            Category category = categoryRepository.getReference(toCategoryId(request.getCategoryId()));

            // Creating a seller item is atomic: item metadata and auction session are saved
            // in the same transaction, so a partial product cannot be left without a session.
            AuctionItem item = AuctionItem.builder()
                    .seller(seller)
                    .category(category)
                    .itemName(request.getName().trim())
                    .description(request.getDescription().trim())
                    .condition(AuctionItem.ItemCondition.GOOD)
                    .status(AuctionItem.ItemStatus.APPROVED)
                    .build();

            item = auctionItemRepository.save(item);

            AuctionSession auction = AuctionSession.builder()
                    .item(item)
                    .startingPrice(request.getStartPrice())
                    .currentPrice(request.getStartPrice())
                    .minBidStep(DEFAULT_MIN_BID_STEP)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .status(initialAuctionStatus(request.getStartTime()))
                    .build();

            return toDto(auctionSessionRepository.save(auction));
        });
    }

    public SellerItemDto updateItem(UpdateSellerItemRequest request) {
        validateUpdate(request);

        return DbExecutor.runAndReturn(() -> {
            AuctionSession auction = findOwnedAuction(request.getSellerId(), request.getItemId(), request.getAuctionId());
            ensureEditable(auction);

            categoryRepository.findById(toCategoryId(request.getCategoryId()))
                    .orElseThrow(() -> new SellerException("Category not found: " + request.getCategoryId()));

            AuctionItem item = auction.getItem();
            item.setCategory(categoryRepository.getReference(toCategoryId(request.getCategoryId())));
            item.setItemName(request.getName().trim());
            item.setDescription(request.getDescription().trim());

            auction.setStartingPrice(request.getStartPrice());
            auction.setCurrentPrice(request.getStartPrice());
            auction.setStartTime(request.getStartTime());
            auction.setEndTime(request.getEndTime());
            auction.setStatus(initialAuctionStatus(request.getStartTime()));

            auctionItemRepository.save(item);
            return toDto(auctionSessionRepository.save(auction));
        });
    }

    public void deleteItem(DeleteSellerItemRequest request) {
        if (request == null || request.itemId() <= 0) {
            throw new SellerException("Item id is required");
        }
        validateSellerId(request.sellerId());

        DbExecutor.run(() -> {
            AuctionSession auction = findAuctionForDelete(request);
            if (!auction.getItem().getSeller().getId().equals(request.sellerId())) {
                throw new SellerException("You can only delete your own items");
            }
            ensureEditable(auction);

            // Soft delete: keep historical consistency for bids/payments and hide it from seller workflows.
            auction.setStatus(AuctionSession.AuctionStatus.CANCELLED);
            auction.getItem().setStatus(AuctionItem.ItemStatus.ARCHIVED);
            auctionItemRepository.save(auction.getItem());
            auctionSessionRepository.save(auction);
        });
    }

    private AuctionSession findOwnedAuction(long sellerId, long itemId, long auctionId) {
        validateSellerId(sellerId);
        if (auctionId <= 0 && itemId <= 0) {
            throw new SellerException("Auction id or item id is required");
        }

        AuctionSession auction = auctionId > 0
                ? auctionSessionRepository.findByIdWithDetails(auctionId)
                        .orElseThrow(() -> new SellerException("Auction not found: " + auctionId))
                : auctionItemRepository.findByIdWithDetails(itemId)
                        .map(AuctionItem::getAuctionSession)
                        .orElseThrow(() -> new SellerException("Item not found: " + itemId));

        if (auction == null || auction.getItem() == null) {
            throw new SellerException("Auction session not found for item");
        }
        if (!auction.getItem().getSeller().getId().equals(sellerId)) {
            throw new SellerException("You can only manage your own items");
        }
        return auction;
    }

    private AuctionSession findAuctionForDelete(DeleteSellerItemRequest request) {
        if (request.auctionId() != null && request.auctionId() > 0) {
            return auctionSessionRepository.findByIdWithDetails(request.auctionId())
                    .orElseThrow(() -> new SellerException("Auction not found: " + request.auctionId()));
        }
        return auctionItemRepository.findByIdWithDetails(request.itemId())
                .map(AuctionItem::getAuctionSession)
                .orElseThrow(() -> new SellerException("Item not found: " + request.itemId()));
    }

    private void ensureEditable(AuctionSession auction) {
        // Once bidding has started, changing product details or removing the auction can
        // invalidate buyer expectations and bid history. Keep the first version conservative.
        if (auction.getTotalBids() != null && auction.getTotalBids() > 0) {
            throw new SellerException("Cannot change an item after it has bids");
        }
        if (auction.getStatus() == AuctionSession.AuctionStatus.ENDED) {
            throw new SellerException("Cannot change an ended auction");
        }
    }

    private SellerItemDto toDto(AuctionSession auction) {
        // Seller UI needs one row containing both product fields and auction fields,
        // so this mapper flattens AuctionItem + AuctionSession into SellerItemDto.
        SellerItemDto dto = new SellerItemDto();
        dto.setAuctionId(valueOrZero(auction.getAuctionId()));
        dto.setStartPrice(auction.getStartingPrice());
        dto.setCurrentPrice(auction.getCurrentPrice());
        dto.setStartTime(auction.getStartTime());
        dto.setEndTime(auction.getEndTime());
        dto.setStatus(auction.getStatus() != null ? auction.getStatus().name() : null);

        AuctionItem item = auction.getItem();
        if (item != null) {
            dto.setItemId(valueOrZero(item.getItemId()));
            dto.setName(item.getItemName());
            dto.setDescription(item.getDescription());
            if (item.getCategory() != null) {
                dto.setCategoryId(item.getCategory().getCategoryId());
                dto.setCategoryName(item.getCategory().getCategoryName());
            }
        }
        return dto;
    }

    private AuctionSession.AuctionStatus initialAuctionStatus(LocalDateTime startTime) {
        return LocalDateTime.now().isBefore(startTime)
                ? AuctionSession.AuctionStatus.SCHEDULED
                : AuctionSession.AuctionStatus.ACTIVE;
    }

    private void validateCreate(CreateSellerItemRequest request) {
        if (request == null) {
            throw new SellerException("Create item request is required");
        }
        validateSellerId(request.getSellerId());
        if (request.getCategoryId() <= 0) {
            throw new SellerException("Category id is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new SellerException("Item name is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new SellerException("Description is required");
        }
        if (request.getStartPrice() == null || request.getStartPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SellerException("Start price must be greater than 0");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new SellerException("Start time and end time are required");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new SellerException("End time must be after start time");
        }
    }

    private void validateUpdate(UpdateSellerItemRequest request) {
        validateCreate(request);
        if (request.getItemId() <= 0 && request.getAuctionId() <= 0) {
            throw new SellerException("Item id or auction id is required");
        }
    }

    private void validateSellerId(long sellerId) {
        if (sellerId <= 0) {
            throw new SellerException("Seller id is required");
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new SellerException("Page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new SellerException("Size must be between 1 and 100");
        }
    }

    private int toCategoryId(long categoryId) {
        if (categoryId > Integer.MAX_VALUE) {
            throw new SellerException("Category id is too large");
        }
        return (int) categoryId;
    }

    private long valueOrZero(Long value) {
        return value != null ? value : 0L;
    }
}
