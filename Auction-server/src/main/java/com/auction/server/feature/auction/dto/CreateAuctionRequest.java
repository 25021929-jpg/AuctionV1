package com.auction.server.feature.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateAuctionRequest {

    private int sellerId;
    private int categoryId;

    private String itemName;
    private String description;

    private BigDecimal startingPrice;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public CreateAuctionRequest() {
    }

    public CreateAuctionRequest(
            int sellerId,
            int categoryId,
            String itemName,
            String description,
            BigDecimal startingPrice,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
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
}