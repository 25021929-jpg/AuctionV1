package com.auction.shared.dto.auction;

import java.time.LocalDateTime;

/** DTO chi tiết phiên đấu giá dùng cho màn chi tiết/live bidding. */
public class AuctionDetailDto extends AuctionSummaryDto {
    private String description;
    private String categoryName;
    private String sellerName;
    private String leaderUsername;
    private LocalDateTime createdAt;

    public AuctionDetailDto() {
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getLeaderUsername() { return leaderUsername; }
    public void setLeaderUsername(String leaderUsername) { this.leaderUsername = leaderUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


    /** Alias cho client cũ: startPrice == startingPrice. */
    public java.math.BigDecimal getStartPrice() { return getStartingPrice(); }
    public void setStartPrice(java.math.BigDecimal startPrice) { setStartingPrice(startPrice); }
    public void setStartPrice(double startPrice) { setStartingPrice(startPrice); }
}
