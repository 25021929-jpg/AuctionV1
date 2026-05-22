package com.auction.server.feature.auction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *là cái khuôn mà server dùng để ánh xạ JSON từ client gửi lên thành object Java.
 */
public class CreateAuctionRequest {

    private int itemId;
    private int sellerId;
    private BigDecimal startingPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public CreateAuctionRequest() {}

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    /** Validate cơ bản trước khi xử lý */
    public void validate() {
        if (itemId <= 0) throw new com.auction.server.feature.auction.AuctionException("itemId không hợp lệ");
        if (sellerId <= 0) throw new com.auction.server.feature.auction.AuctionException("sellerId không hợp lệ");
        if (startingPrice == null || startingPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new com.auction.server.feature.auction.AuctionException("Giá khởi điểm phải lớn hơn 0");
        if (startTime == null || endTime == null)
            throw new com.auction.server.feature.auction.AuctionException("Thời gian không được để trống");
        if (!endTime.isAfter(startTime))
            throw new com.auction.server.feature.auction.AuctionException("Thời gian kết thúc phải sau thời gian bắt đầu");
        if (startTime.isBefore(LocalDateTime.now()))
            throw new com.auction.server.feature.auction.AuctionException("Thời gian bắt đầu phải ở tương lai");
    }
}
