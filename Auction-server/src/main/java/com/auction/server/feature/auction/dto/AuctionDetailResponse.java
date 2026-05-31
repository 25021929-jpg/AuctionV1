package com.auction.server.feature.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về danh sách phiên đấu giá.
 */

public class AuctionDetailResponse {

    private Long auctionId;
    private Long itemId;
    private String itemName;
    private String description;
    private String categoryName;
    private String sellerName;
    private String leaderUsername;

    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal minBidStep;
    private Integer totalBids;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;

    public AuctionDetailResponse() {
    }

    public AuctionDetailResponse(
            Long auctionId,
            Long itemId,
            String itemName,
            String description,
            String categoryName,
            String sellerName,
            BigDecimal startingPrice,
            BigDecimal currentPrice,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status
    ) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.categoryName = categoryName;
        this.sellerName = sellerName;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getLeaderUsername() {
        return leaderUsername;
    }

    public void setLeaderUsername(String leaderUsername) {
        this.leaderUsername = leaderUsername;
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