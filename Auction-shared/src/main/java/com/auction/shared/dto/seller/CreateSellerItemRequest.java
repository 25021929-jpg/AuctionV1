package com.auction.shared.dto.seller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Request Seller tạo sản phẩm/phiên đấu giá. */
public class CreateSellerItemRequest {
    private long sellerId;
    private long categoryId;
    private String name;
    private String description;
    private BigDecimal startPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public CreateSellerItemRequest() {
    }

    public long getSellerId() { return sellerId; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getStartPrice() { return startPrice; }
    public void setStartPrice(BigDecimal startPrice) { this.startPrice = startPrice; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
