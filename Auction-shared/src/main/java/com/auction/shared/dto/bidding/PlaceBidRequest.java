package com.auction.shared.dto.bidding;

import java.math.BigDecimal;

/**
 * Request đặt giá. Giữ alias auctionSessionId/bidAmount để client-server cũ dễ map khi tích hợp.
 */
public class PlaceBidRequest {
  private long auctionId;
  private long auctionSessionId;
  private long bidderId;
  private BigDecimal amount;
  private BigDecimal bidAmount;

  public PlaceBidRequest() {}

  public PlaceBidRequest(long auctionId, long bidderId, BigDecimal amount) {
    this.auctionId = auctionId;
    this.auctionSessionId = auctionId;
    this.bidderId = bidderId;
    setAmount(amount);
  }

  public long getAuctionId() {
    return auctionId;
  }

  public void setAuctionId(long auctionId) {
    this.auctionId = auctionId;
    this.auctionSessionId = auctionId;
  }

  public long getAuctionSessionId() {
    return auctionSessionId;
  }

  public void setAuctionSessionId(long auctionSessionId) {
    this.auctionSessionId = auctionSessionId;
  }

  public long getBidderId() {
    return bidderId;
  }

  public void setBidderId(long bidderId) {
    this.bidderId = bidderId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
    this.bidAmount = amount;
  }

  public BigDecimal getBidAmount() {
    return bidAmount;
  }

  public void setBidAmount(BigDecimal bidAmount) {
    this.bidAmount = bidAmount;
  }
}
