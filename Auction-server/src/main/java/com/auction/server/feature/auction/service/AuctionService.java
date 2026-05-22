package com.auction.server.feature.auction.service;

import com.auction.server.feature.auction.AuctionException;
import com.auction.server.feature.auction.AuctionStatus;
import com.auction.server.feature.auction.dto.AuctionDetailResponse;
import com.auction.server.feature.auction.dto.AuctionResponse;
import com.auction.server.feature.auction.dto.CreateAuctionRequest;
import com.auction.server.feature.auction.repository.AuctionRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service xử lý nghiệp vụ auction.
 * Không biết gì về Socket hay Controller — chỉ lo logic + gọi Repository.
 */
public class AuctionService {

    private final AuctionRepository auctionRepository;

    public AuctionService() {
        this.auctionRepository = new AuctionRepository();
    }

    // ===================================================================
    // LẤY DANH SÁCH
    // ===================================================================

    public List<AuctionResponse> getAllAuctions() {
        return auctionRepository.findAll();
    }

    // ===================================================================
    // LẤY CHI TIẾT
    // ===================================================================

    public AuctionDetailResponse getAuctionDetail(int auctionId) {
        if (auctionId <= 0) {
            throw new AuctionException("auctionId không hợp lệ");
        }
        AuctionDetailResponse detail = auctionRepository.findDetailById(auctionId);

        // [FIX] remainingSeconds là nghiệp vụ → tính ở Service, không phải Repository
        long remaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), detail.getEndTime());
        detail.setRemainingSeconds(Math.max(0, remaining));

        return detail;
    }

    // ===================================================================
    // TẠO PHIÊN ĐẤU GIÁ
    // ===================================================================

    public AuctionResponse createAuction(CreateAuctionRequest request) {
        request.validate();
        return auctionRepository.create(
                request.getItemId(),
                request.getSellerId(),
                request.getStartingPrice(),
                request.getStartTime(),
                request.getEndTime()
        );
    }

    // ===================================================================
    // CHUYỂN TRẠNG THÁI (gọi bởi AuctionScheduler)
    // ===================================================================

    /**
     * OPEN → RUNNING.
     * Gộp check + update thành 1 query → tránh race condition, giảm round-trip.
     */
    public void startAuction(int auctionId) {
        int rows = auctionRepository.updateStatusConditional(
                auctionId, AuctionStatus.OPEN, AuctionStatus.RUNNING);
        if (rows == 0) {
            throw new AuctionException(
                    "Phiên auction_id=" + auctionId + " không ở trạng thái OPEN, bỏ qua.");
        }
    }

    /**
     * RUNNING → FINISHED / CANCELED.
     * Vẫn cần đọc detail 1 lần để biết có bid không (xác định FINISHED vs CANCELED).
     */
    public void finishAuction(int auctionId) {
        AuctionDetailResponse detail = auctionRepository.findDetailById(auctionId);
        if (!AuctionStatus.RUNNING.name().equals(detail.getStatus())) {
            throw new AuctionException(
                    "Phiên auction_id=" + auctionId + " không đang RUNNING, bỏ qua.");
        }
        boolean hasBid = detail.getCurrentWinnerId() != null;
        auctionRepository.updateStatusToFinished(auctionId, hasBid);
    }

    /**
     * Hủy phiên (Admin) — chấp nhận cả OPEN lẫn RUNNING.
     */
    public void cancelAuction(int auctionId) {
        AuctionDetailResponse detail = auctionRepository.findDetailById(auctionId);
        String status = detail.getStatus();
        if (!AuctionStatus.OPEN.name().equals(status)
                && !AuctionStatus.RUNNING.name().equals(status)) {
            throw new AuctionException("Chỉ có thể hủy phiên đang OPEN hoặc RUNNING, hiện tại: " + status);
        }
        auctionRepository.updateStatusDirect(auctionId, AuctionStatus.CANCELED);
    }

    /** Gia hạn phiên thêm extraSeconds giây (Anti-sniping) */
    public void extendAuction(int auctionId, long extraSeconds) {
        AuctionDetailResponse detail = auctionRepository.findDetailById(auctionId);
        auctionRepository.extendEndTime(auctionId,
                detail.getEndTime().plusSeconds(extraSeconds));
    }
}
