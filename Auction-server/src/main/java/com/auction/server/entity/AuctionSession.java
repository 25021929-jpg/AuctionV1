package com.auction.server.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "auction_sessions")
public class AuctionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long auctionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private AuctionItem item;

    @Column(name = "starting_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal startingPrice;

    @Column(name = "current_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "min_bid_step", nullable = false, precision = 15, scale = 2)
    private BigDecimal minBidStep = BigDecimal.valueOf(1000);

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(name = "total_bids", nullable = false)
    private Integer totalBids = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private AuctionStatus status = AuctionStatus.SCHEDULED;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "auctionSession", fetch = FetchType.LAZY)
    private List<Bid> bids = new ArrayList<>();

    @OneToOne(mappedBy = "auctionSession", fetch = FetchType.LAZY)
    private Payment payment;

    public AuctionSession() {
    }

    public boolean isActive() {
        return status == AuctionStatus.ACTIVE && LocalDateTime.now().isBefore(endTime);
    }

    public boolean canAcceptBid(BigDecimal amount) {
        return isActive() && amount.compareTo(currentPrice.add(minBidStep)) >= 0;
    }

    public void applyNewBid(BigDecimal amount, User bidder) {
        this.currentPrice = amount;
        this.winner = bidder;
        this.totalBids = this.totalBids + 1;
    }

    public Long getAuctionId() { return auctionId; }
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }
    public AuctionItem getItem() { return item; }
    public void setItem(AuctionItem item) { this.item = item; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getMinBidStep() { return minBidStep; }
    public void setMinBidStep(BigDecimal minBidStep) { this.minBidStep = minBidStep; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public User getWinner() { return winner; }
    public void setWinner(User winner) { this.winner = winner; }
    public Integer getTotalBids() { return totalBids; }
    public void setTotalBids(Integer totalBids) { this.totalBids = totalBids; }
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Bid> getBids() { return bids; }
    public void setBids(List<Bid> bids) { this.bids = bids; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuctionSession that)) return false;
        return Objects.equals(auctionId, that.auctionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionId);
    }

    public enum AuctionStatus {
        SCHEDULED, ACTIVE, ENDED, CANCELED
    }
}
