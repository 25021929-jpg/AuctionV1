package com.auction.server.feature.bidding.dto;

import java.math.BigDecimal;

/**
 * Request để đặt giá cho phiên đấu giá.
 *
 * ✅ Dùng Long cho ID (max ~9 tỷ tỷ)
 * ✅ Dùng BigDecimal cho tiền (chính xác 100%)
 */
public class PlaceBidRequest {

    private Long auctionSessionId;    // ✅ Changed: int → Long
    private Long bidderId;            // ✅ Changed: int → Long
    private BigDecimal bidAmount;     // ✅ Changed: double → BigDecimal

    public PlaceBidRequest() {
    }

    public PlaceBidRequest(Long auctionSessionId, Long bidderId, BigDecimal bidAmount) {
        this.auctionSessionId = auctionSessionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
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
}