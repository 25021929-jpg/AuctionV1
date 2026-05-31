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
import com.auction.shared.dto.category.CategoryDto;

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

        refreshAuctionStatuses();
        return DbExecutor.query(() ->
                auctionSessionRepository.findActive(page, size)
                        .stream()
                        .map(this::toSummaryDto) // map trong session — tránh LazyInit
                        .toList()
        );
    }


    /**
     * Lấy danh mục sản phẩm trực tiếp từ bảng categories.
     *
     * <p>Client dùng dữ liệu này để render ComboBox chọn danh mục. Không hard-code
     * categoryId/categoryName ở giao diện vì dữ liệu thật nằm ở database.</p>
     */
    public List<CategoryDto> listCategories() {
        return DbExecutor.query(() ->
                categoryRepository.findAllSorted()
                        .stream()
                        .map(this::toCategoryDto)
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

        refreshAuctionStatuses();
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
            AuctionItem item = new AuctionItem();
            item.setSeller(seller);
            item.setCategory(categoryRepository.getReference(request.getCategoryId()));
            item.setItemName(request.getItemName().trim());
            item.setDescription(request.getDescription().trim());
            item.setStatus(AuctionItem.ItemStatus.DRAFT);
            item.setCondition(AuctionItem.ItemCondition.GOOD);

            item = auctionItemRepository.save(item);

            // currentPrice = startingPrice ban đầu, tăng dần khi có bid
            AuctionSession auction = new AuctionSession();
            auction.setItem(item);
            auction.setStartingPrice(request.getStartingPrice());
            auction.setCurrentPrice(request.getStartingPrice());
            auction.setStartTime(request.getStartTime());
            auction.setEndTime(request.getEndTime());
            auction.setStatus(initialAuctionStatus(request.getStartTime(), request.getEndTime()));

            AuctionSession saved = auctionSessionRepository.save(auction);

            // map trong session — tránh LazyInitializationException
            return toSummaryDto(saved);
        });
    }


    /**
     * Đồng bộ trạng thái phiên trước khi đọc danh sách/chi tiết.
     *
     * Dự án hiện không có scheduler chạy nền ổn định, nên nếu không làm bước này
     * phiên đã tới giờ vẫn nằm SCHEDULED và người dùng không thể đặt giá.
     */
    private void refreshAuctionStatuses() {
        DbExecutor.runAndReturn(() -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            List<AuctionSession> scheduledToStart = auctionSessionRepository.findScheduledToStart();

            List<Long> shouldStart = scheduledToStart.stream()
                    .filter(a -> a.getEndTime() != null && a.getEndTime().isAfter(now))
                    .map(AuctionSession::getAuctionId)
                    .toList();
            if (!shouldStart.isEmpty()) {
                auctionSessionRepository.bulkUpdateStatus(shouldStart, AuctionSession.AuctionStatus.ACTIVE);
            }

            /*
             * Không chuyển ACTIVE/SCHEDULED sang ENDED tại đây nữa.
             * Việc kết thúc phiên phải đi qua AuctionStatusScheduler để đồng thời
             * chốt ví: trừ số dư winner, cộng số dư seller và ghi lịch sử giao dịch.
             * Nếu đọc danh sách mà tự bulk-update status ở đây thì tiền sẽ không được
             * xử lý, gây lệch dữ liệu.
             */
            return null;
        });
    }

    private AuctionSession.AuctionStatus initialAuctionStatus(java.time.LocalDateTime startTime,
                                                              java.time.LocalDateTime endTime) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (endTime != null && !endTime.isAfter(now)) {
            return AuctionSession.AuctionStatus.ENDED;
        }
        if (startTime != null && !startTime.isAfter(now)) {
            return AuctionSession.AuctionStatus.ACTIVE;
        }
        return AuctionSession.AuctionStatus.SCHEDULED;
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
        java.time.LocalDateTime now = java.time.LocalDateTime.now().withSecond(0).withNano(0);
        /*
         * Nếu startTime <= now và endTime còn ở tương lai, phiên hợp lệ và sẽ
         * được tạo ở trạng thái ACTIVE. Điều kiện bắt buộc là startTime < endTime.
         */
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new AuctionException("End time must be after start time");
        }
        if (!request.getEndTime().isAfter(now)) {
            throw new AuctionException("End time must be after current time");
        }
    }

    // =====================================================
    // PRIVATE — MAPPING
    // =====================================================


    private CategoryDto toCategoryDto(com.auction.server.entity.Category category) {
        Long parentId = category.getParent() == null || category.getParent().getCategoryId() == null
                ? null
                : category.getParent().getCategoryId().longValue();
        return new CategoryDto(
                category.getCategoryId() == null ? 0L : category.getCategoryId().longValue(),
                category.getCategoryName(),
                category.getSlug(),
                parentId
        );
    }

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
        dto.setMinBidStep(a.getMinBidStep());
        dto.setTotalBids(a.getTotalBids());
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
        if (a.getWinner() != null) {
            dto.setLeaderUsername(a.getWinner().getUsername());
        }
        dto.setStartingPrice(a.getStartingPrice());
        dto.setCurrentPrice(a.getCurrentPrice());
        dto.setMinBidStep(a.getMinBidStep());
        dto.setTotalBids(a.getTotalBids());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        return dto;
    }
}
