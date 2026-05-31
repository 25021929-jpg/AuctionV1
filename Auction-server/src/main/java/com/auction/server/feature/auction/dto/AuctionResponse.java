package com.auction.server.feature.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về danh sách phiên đấu giá.
 *
 * ✅ Dùng Long cho ID
 * ✅ Dùng BigDecimal cho giá
 */
public class AuctionResponse {

    private Long auctionId;              // ✅ Changed: int → Long
    private Long itemId;                 // ✅ Changed: int → Long
    private String itemName;
    private BigDecimal startingPrice;    // ✅ Đã đúng - BigDecimal
    private BigDecimal currentPrice;     // ✅ Đã đúng - BigDecimal
    private BigDecimal minBidStep;
    private Integer totalBids;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public AuctionResponse() {
    }

    public AuctionResponse(
            Long auctionId,                 // ✅ Changed
            Long itemId,                    // ✅ Changed
            String itemName,
            BigDecimal startingPrice,
            BigDecimal currentPrice,
            BigDecimal minBidStep,
            Integer totalBids,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status
    ) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.minBidStep = minBidStep;
        this.totalBids = totalBids;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // Getters & Setters
    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getMinBidStep() {
        return minBidStep;
    }

    public void setMinBidStep(BigDecimal minBidStep) {
        this.minBidStep = minBidStep;
    }

    public Integer getTotalBids() {
        return totalBids;
    }

    public void setTotalBids(Integer totalBids) {
        this.totalBids = totalBids;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}