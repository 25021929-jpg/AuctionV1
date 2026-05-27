package com.auction.server.feature.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request để tạo phiên đấu giá.
 *
 * ✅ Dùng Long cho ID (max ~9 tỷ tỷ)
 * ✅ Đã dùng BigDecimal cho giá (đúng)
 */
public class CreateAuctionRequest {

    private Long sellerId;          // ✅ Changed: int → Long
    private Integer categoryId;     // ✅ Changed: int → Integer (category dùng Integer)

    private String itemName;
    private String description;

    private BigDecimal startingPrice;  // ✅ Đã đúng - BigDecimal

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public CreateAuctionRequest() {
    }

    public CreateAuctionRequest(
            Long sellerId,                  // ✅ Changed
            Integer categoryId,             // ✅ Changed
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

    // Getters & Setters
    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
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