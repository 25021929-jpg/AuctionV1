package com.auction.server.feature.bidding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO để trả về dữ liệu bid đã được tạo.
 *
 * ✅ Dùng Long cho ID (max ~9 tỷ tỷ) thay vì int (max ~2 tỷ)
 * ✅ Dùng BigDecimal cho tiền (chính xác 100%) thay vì double (lỗi dấu phẩy)
 * ✅ Loại bỏ message (là metadata của Response wrapper, không phải domain data)
 * ✅ Thêm bidTime và isWinning để client biết thời gian và trạng thái
 */
public class BidResponse {

    private Long bidId;                    // ✅ Changed: int → Long
    private Long auctionSessionId;         // ✅ Changed: int → Long
    private Long bidderId;                 // ✅ Changed: int → Long
    private BigDecimal bidAmount;          // ✅ Changed: double → BigDecimal
    private LocalDateTime bidTime;         // ✅ Added: thêm thời gian đặt giá
    private Boolean isWinning;             // ✅ Added: flag winning bid

    public BidResponse() {
    }

    public BidResponse(Long bidId, Long auctionSessionId, Long bidderId,
                       BigDecimal bidAmount, LocalDateTime bidTime, Boolean isWinning) {
        this.bidId = bidId;
        this.auctionSessionId = auctionSessionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
        this.isWinning = isWinning;
    }

    // Getters & Setters
    public Long getBidId() {
        return bidId;
    }

    public void setBidId(Long bidId) {
        this.bidId = bidId;
    }

    public Long getAuctionSessionId() {
        return auctionSessionId;
    }

    public void setAuctionSessionId(Long auctionSessionId) {
        this.auctionSessionId = auctionSessionId;
    }

    public Long getBidderId() {
        return bidderId;
    }

    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }

    public Boolean getIsWinning() {
        return isWinning;
    }

    public void setIsWinning(Boolean isWinning) {
        this.isWinning = isWinning;
    }
}