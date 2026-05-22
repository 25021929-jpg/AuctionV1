package com.auction.server.feature.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về chi tiết 1 phiên đấu giá (kèm lịch sử bid).
 */
public class AuctionDetailResponse {

    private int auctionId;
    private int itemId;
    private String itemName;
    private String itemDescription;
    private String itemCategory;         // ELECTRONICS / ART / VEHICLE
    private int sellerId;
    private String sellerName;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private Integer currentWinnerId;     // null nếu chưa có bid
    private String currentWinnerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private long remainingSeconds;       // giây còn lại
    private List<BidHistoryItem> bidHistory;

    public AuctionDetailResponse() {}

    /** Record nhỏ chứa 1 dòng lịch sử bid */
    public static class BidHistoryItem {
        private int bidId;
        private int bidderId;
        private String bidderName;
        private BigDecimal amount;
        private LocalDateTime bidTime;

        //lsu đấu giá
        public BidHistoryItem(int bidId, int bidderId, String bidderName,
                              BigDecimal amount, LocalDateTime bidTime) {
            this.bidId = bidId;
            this.bidderId = bidderId;
            this.bidderName = bidderName;
            this.amount = amount;
            this.bidTime = bidTime;
        }

        public int getBidId() { return bidId; }
        public int getBidderId() { return bidderId; }
        public String getBidderName() { return bidderName; }
        public BigDecimal getAmount() { return amount; }
        public LocalDateTime getBidTime() { return bidTime; }
    }

    // --- Getters & Setters ---
    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public String getItemCategory() { return itemCategory; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public Integer getCurrentWinnerId() { return currentWinnerId; }
    public void setCurrentWinnerId(Integer currentWinnerId) { this.currentWinnerId = currentWinnerId; }

    public String getCurrentWinnerName() { return currentWinnerName; }
    public void setCurrentWinnerName(String currentWinnerName) { this.currentWinnerName = currentWinnerName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(long remainingSeconds) { this.remainingSeconds = remainingSeconds; }

    public List<BidHistoryItem> getBidHistory() { return bidHistory; }
    public void setBidHistory(List<BidHistoryItem> bidHistory) { this.bidHistory = bidHistory; }
}
