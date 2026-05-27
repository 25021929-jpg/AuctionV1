package com.auction.server.feature.bidding.repository;

import com.auction.server.entity.AuctionSession;

import java.util.List;
import java.util.Optional;

/**
 * Interface định nghĩa contract của AuctionSessionRepository.
 *
 * Service chỉ biết interface này — không biết implementation.
 * Chữ ký hàm KHÔNG có Session, KHÔNG có Hibernate object.
 * Session được quản lý ngầm qua Thread-bound mechanism.
 */
public interface AuctionSessionRepository {

    // ===== READ =====
    Optional<AuctionSession> findById(Long id);
    Optional<AuctionSession> findByIdWithDetails(Long id);
    List<AuctionSession> findActive(int page, int size);
    List<AuctionSession> findExpired();
    List<AuctionSession> findScheduledToStart();
    List<AuctionSession> findBySeller(Long sellerId, int page, int size);

    // ===== READ WITH LOCK =====
    // Chữ ký sạch — không có Session tham số
    // Lock được thiết lập ngầm trên Thread-bound session
    Optional<AuctionSession> findByIdWithLock(Long id);

    // ===== WRITE =====
    AuctionSession save(AuctionSession auction);
    int bulkUpdateStatus(List<Long> ids, AuctionSession.AuctionStatus status);
}