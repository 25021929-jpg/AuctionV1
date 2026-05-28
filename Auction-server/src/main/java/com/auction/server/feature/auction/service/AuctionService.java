package com.auction.server.feature.auction.service;

import com.auction.server.database.DbExecutor;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.entity.AuctionItem;
import com.auction.server.entity.AuctionSession;
import com.auction.server.entity.User;
import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.dto.AuctionResponse;
import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.repository.AuctionItemRepository;
import com.auction.server.feature.auction.repository.AuctionSessionRepository;
import com.auction.server.feature.auction.repository.CategoryRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * AuctionService: xử lý nghiệp vụ phiên đấu giá.
 *
 * Nguyên tắc thiết kế:
 *   - Không biết về Session/Transaction — DbExecutor lo phần đó
 *   - Không tự new repository — nhận qua constructor (DIP)
 *   - validate format trước transaction — không tốn DB connection khi input sai
 *   - toDto() map bên trong session — tránh LazyInitializationException
 *
 * Exception strategy (đơn giản hóa):
 *   - Chỉ dùng AuctionException cho mọi lỗi nghiệp vụ
 *   - Không dùng DataNotFoundException/DuplicateDataException hierarchy
 *   - DbExecutor tự ném DatabaseException nếu lỗi hạ tầng — không cần catch thêm
 *
 * NOTE: Do not instantiate repository implementations here.
 * Use constructor injection from composition root (Main) to preserve DIP.
 */
public class AuctionService {

    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionItemRepository    auctionItemRepository;
    private final CategoryRepository       categoryRepository;
    private final UserRepository           userRepository;

    public AuctionService(
            AuctionSessionRepository auctionSessionRepository,
            AuctionItemRepository    auctionItemRepository,
            CategoryRepository       categoryRepository,
            UserRepository           userRepository
    ) {
        this.auctionSessionRepository = auctionSessionRepository;
        this.auctionItemRepository    = auctionItemRepository;
        this.categoryRepository       = categoryRepository;
        this.userRepository           = userRepository;
    }

    // =====================================================
    // GET ALL AUCTIONS
    // =====================================================

    /**
     * Lấy danh sách phiên đấu giá đang hoạt động, có phân trang.
     *
     * Dùng DbExecutor.query() — read-only, tắt dirty checking.
     * Không catch DataAccessException — DbExecutor tự xử lý lỗi hạ tầng.
     */
    public List<AuctionResponse> getAllAuctions(int page, int size) {
        // Validate ngoài transaction — không tốn DB connection
        if (page < 0) throw new AuctionException("Page must be >= 0");
        if (size <= 0 || size > 100) throw new AuctionException("Size must be between 1 and 100");

        return DbExecutor.query(() ->
                auctionSessionRepository.findActive(page, size)
                        .stream()
                        .map(this::toSummaryDto) // map trong session — tránh LazyInit
                        .toList()
        );
    }

    // =====================================================
    // GET AUCTION DETAIL
    // =====================================================

    /**
     * Lấy chi tiết 1 phiên đấu giá — trả AuctionDetailResponse đầy đủ.
     *
     * @throws AuctionException nếu auctionId không hợp lệ hoặc không tìm thấy
     */
    public AuctionDetailResponse getAuctionDetail(Long auctionId) {
        // Validate ngoài transaction
        if (auctionId == null || auctionId <= 0) {
            throw new AuctionException("Invalid auction id");
        }

        return DbExecutor.query(() ->
                auctionSessionRepository.findByIdWithDetails(auctionId)
                        .map(this::toDetailDto)  // map trong session — tránh LazyInit
                        .orElseThrow(() -> new AuctionException(
                                "Auction not found: id = " + auctionId))
        );
    }

    // =====================================================
    // CREATE AUCTION
    // =====================================================

    /**
     * Tạo phiên đấu giá mới.
     *
     * Luồng:
     *   1. validate format   — ngoài transaction
     *   2. [TX] kiểm tra category + tạo AuctionItem + tạo AuctionSession — atomic
     *      → nếu save AuctionSession lỗi thì AuctionItem cũng rollback
     *
     * @throws AuctionException nếu input không hợp lệ hoặc category không tồn tại
     */
    public AuctionResponse createAuction(CreateAuctionRequest request) {
        // Validate format — ngoài transaction
        validateCreateAuction(request);

        return DbExecutor.runAndReturn(() -> {

            // Kiểm tra category tồn tại — trong transaction cùng với save
            // → atomic: kiểm tra và tạo không thể bị tách rời
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AuctionException(
                            "Category not found: id = " + request.getCategoryId()));

            // getReference(): proxy, không hit DB — chỉ dùng để set FK
            User seller = userRepository.getReference(request.getSellerId());

            // Tạo AuctionItem trước — AuctionSession phụ thuộc vào Item
            AuctionItem item = AuctionItem.builder()
                    .seller(seller)
                    .category(categoryRepository.getReference(request.getCategoryId()))
                    .itemName(request.getItemName().trim())
                    .description(request.getDescription().trim())
                    .status(AuctionItem.ItemStatus.DRAFT)
                    .condition(AuctionItem.ItemCondition.GOOD)
                    .build();

            item = auctionItemRepository.save(item);

            // currentPrice = startingPrice ban đầu, tăng dần khi có bid
            AuctionSession auction = AuctionSession.builder()
                    .item(item)
                    .startingPrice(request.getStartingPrice())
                    .currentPrice(request.getStartingPrice())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .status(AuctionSession.AuctionStatus.SCHEDULED)
                    .build();

            AuctionSession saved = auctionSessionRepository.save(auction);

            // map trong session — tránh LazyInitializationException
            return toSummaryDto(saved);
        });
    }

    // =====================================================
    // PRIVATE — VALIDATE
    // =====================================================

    /** Validate format request — gọi trước transaction, không tốn DB connection */
    private void validateCreateAuction(CreateAuctionRequest request) {
        if (request == null) {
            throw new AuctionException("Create auction request is required");
        }
        if (request.getSellerId() == null || request.getSellerId() <= 0) {
            throw new AuctionException("Invalid seller id");
        }
        if (request.getCategoryId() == null || request.getCategoryId() <= 0) {
            throw new AuctionException("Invalid category id");
        }
        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new AuctionException("Item name is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new AuctionException("Description is required");
        }
        if (request.getStartingPrice() == null
                || request.getStartingPrice().compareTo(BigDecimal.ZERO) <= 0) {
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

    // =====================================================
    // PRIVATE — MAPPING
    // =====================================================

    /**
     * Map → AuctionResponse (tóm tắt, dùng cho danh sách).
     * PHẢI gọi trong lambda DbExecutor — session còn mở để access lazy relation.
     */
    private AuctionResponse toSummaryDto(AuctionSession a) {
        AuctionResponse dto = new AuctionResponse();
        dto.setAuctionId(a.getAuctionId());
        if (a.getItem() != null) {
            dto.setItemId(a.getItem().getItemId());
            dto.setItemName(a.getItem().getItemName());
        }
        dto.setStartingPrice(a.getStartingPrice());
        dto.setCurrentPrice(a.getCurrentPrice());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        return dto;
    }

    /**
     * Map → AuctionDetailResponse (đầy đủ, dùng cho trang chi tiết).
     * PHẢI gọi trong lambda DbExecutor — session còn mở để access lazy relation.
     */
    private AuctionDetailResponse toDetailDto(AuctionSession a) {
        AuctionDetailResponse dto = new AuctionDetailResponse();
        dto.setAuctionId(a.getAuctionId());
        if (a.getItem() != null) {
            dto.setItemId(a.getItem().getItemId());
            dto.setItemName(a.getItem().getItemName());
            dto.setDescription(a.getItem().getDescription());
            if (a.getItem().getCategory() != null) {
                dto.setCategoryName(a.getItem().getCategory().getCategoryName());
            }
            if (a.getItem().getSeller() != null) {
                dto.setSellerName(a.getItem().getSeller().getFullName());
            }
        }
        dto.setStartingPrice(a.getStartingPrice());
        dto.setCurrentPrice(a.getCurrentPrice());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        return dto;
    }
}