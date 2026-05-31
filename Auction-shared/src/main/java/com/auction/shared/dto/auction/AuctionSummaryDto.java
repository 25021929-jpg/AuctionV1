package com.auction.shared.dto.auction;

import com.auction.shared.domain.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO tóm tắt phiên đấu giá dùng cho màn danh sách. */
public class AuctionSummaryDto {
    private long auctionId;
    private long itemId;
    private String itemName;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal minBidStep;
    private int totalBids;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;

    public AuctionSummaryDto() {
    }

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }
    public long getItemId() { return itemId; }
    public void setItemId(long itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = BigDecimal.valueOf(startingPrice); }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = BigDecimal.valueOf(currentPrice); }
    public BigDecimal getMinBidStep() { return minBidStep; }
    public void setMinBidStep(BigDecimal minBidStep) { this.minBidStep = minBidStep; }
    public void setMinBidStep(double minBidStep) { this.minBidStep = BigDecimal.valueOf(minBidStep); }
    public int getTotalBids() { return totalBids; }
    public void setTotalBids(int totalBids) { this.totalBids = totalBids; }
    public void setTotalBids(Integer totalBids) { this.totalBids = totalBids == null ? 0 : totalBids; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    public void setStatus(String status) { this.status = AuctionStatus.fromString(status); }

    /** Dùng cho JavaFX TableColumn mà không bind trực tiếp enum. */
    public String getStatusText() {
        return status == null ? "-" : status.name();
    }

    public double getCurrentPriceAsDouble() {
        return currentPrice == null ? 0.0 : currentPrice.doubleValue();
    }
}
