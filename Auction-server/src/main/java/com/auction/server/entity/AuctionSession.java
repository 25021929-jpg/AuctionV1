package com.auction.server.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "auction_sessions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"bids", "payment"})
@EqualsAndHashCode(of = {"auctionId"})
public class AuctionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long auctionId;

    // ===== 1:1 với AuctionItem =====
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "item_id",
            nullable = false,
            unique = true               // UNIQUE: 1 item chỉ có 1 phiên cùng lúc
    )
    private AuctionItem item;

    @Column(name = "starting_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal startingPrice;

    @Column(name = "reserve_price", precision = 15, scale = 2)
    private BigDecimal reservePrice;
    // nullable — không phải phiên nào cũng có giá sàn bí mật

    @Column(name = "current_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "min_bid_step", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal minBidStep = BigDecimal.valueOf(1000);

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    // nullable — chưa có winner cho đến khi kết thúc
    private User winner;

    @Column(name = "total_bids", nullable = false)
    @Builder.Default
    private Integer totalBids = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private AuctionStatus status = AuctionStatus.SCHEDULED;

    @Version
    // @Version: Optimistic Locking
    // Mỗi lần UPDATE, Hibernate tự thêm điều kiện: AND version = ?
    // và tăng version lên 1
    // Nếu 2 thread cùng UPDATE với version cũ → thread đến sau
    // thấy 0 rows affected → throw OptimisticLockException
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "auctionSession", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bid> bids = new ArrayList<>();

    @OneToOne(mappedBy = "auctionSession", fetch = FetchType.LAZY)
    private Payment payment;

    // ===== HELPER METHODS =====
    // Logic nghiệp vụ đơn giản đặt ngay trong Entity

    public boolean isActive() {
        return status == AuctionStatus.ACTIVE
                && LocalDateTime.now().isBefore(endTime);
    }

    public boolean canAcceptBid(BigDecimal amount) {
        // Giá đặt phải >= giá hiện tại + bước giá tối thiểu
        return isActive()
                && amount.compareTo(
                currentPrice.add(minBidStep)
        ) >= 0;
    }

    public void applyNewBid(BigDecimal amount, User bidder) {
        // Cập nhật trạng thái khi có bid mới
        this.currentPrice = amount;
        this.winner       = bidder;
        this.totalBids    = this.totalBids + 1;
        // version tự tăng bởi @Version khi Hibernate flush
    }

    public enum AuctionStatus {
        SCHEDULED,  // chưa bắt đầu
        ACTIVE,     // đang diễn ra
        ENDED,      // đã kết thúc
        CANCELLED   // đã huỷ
    }
}