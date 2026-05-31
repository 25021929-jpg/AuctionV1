package com.auction.shared.dto.bidding;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO kết quả đặt giá thành công, dùng chung cho response và event realtime. */
public class BidResultDto {
  private long bidId;
  private long auctionId;
  private long auctionSessionId;
  private long bidderId;
  private String bidderUsername;
  private BigDecimal bidAmount;
  private BigDecimal newCurrentPrice;
  private String leaderUsername;
  private LocalDateTime bidTime;
  private Boolean isWinning;
  private String message;

  public BidResultDto() {}

  public long getBidId() {
    return bidId;
  }

  public void setBidId(long bidId) {
    this.bidId = bidId;
  }

  public long getAuctionId() {
    return auctionId;
  }

  public void setAuctionId(long auctionId) {
    this.auctionId = auctionId;
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

  public String getBidderUsername() {
    return bidderUsername;
  }

  public void setBidderUsername(String bidderUsername) {
    this.bidderUsername = bidderUsername;
  }

  public BigDecimal getBidAmount() {
    return bidAmount;
  }

  public void setBidAmount(BigDecimal bidAmount) {
    this.bidAmount = bidAmount;
  }

  public BigDecimal getNewCurrentPrice() {
    return newCurrentPrice;
  }

  public void setNewCurrentPrice(BigDecimal newCurrentPrice) {
    this.newCurrentPrice = newCurrentPrice;
  }

  public String getLeaderUsername() {
    return leaderUsername;
  }

  public void setLeaderUsername(String leaderUsername) {
    this.leaderUsername = leaderUsername;
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

  public void setIsWinning(Boolean winning) {
    isWinning = winning;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
