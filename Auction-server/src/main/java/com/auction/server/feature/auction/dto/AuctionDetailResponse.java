package com.auction.server.feature.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionDetailResponse {

    private int auctionId;
    private int itemId;
    private String itemName;
    private String description;     //mô tả chi tiết món đồ
    private String categoryName;    //tên danh mục
    private String sellerName;

    private BigDecimal startingPrice;
    private BigDecimal currentPrice;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;

    public AuctionDetailResponse() {
    }

    public AuctionDetailResponse(
            int auctionId,
            int itemId,
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

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
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