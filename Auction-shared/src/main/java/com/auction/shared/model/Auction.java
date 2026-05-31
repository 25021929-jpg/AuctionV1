package com.auction.shared.model;

import com.auction.shared.domain.AuctionStatus;
import java.io.Serializable;

/**
 * Shared auction model (minimal). Client currently uses DTOs; server can use this or separate
 * entity.
 */
public class Auction implements Serializable {
  private long id;
  private String itemName;
  private String description;
  private double startingPrice;
  private double currentPrice;
  private long startTimeEpochMs;
  private long endTimeEpochMs;
  private AuctionStatus status;

  public Auction() {}

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public double getStartingPrice() {
    return startingPrice;
  }

  public void setStartingPrice(double startingPrice) {
    this.startingPrice = startingPrice;
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(double currentPrice) {
    this.currentPrice = currentPrice;
  }

  public long getStartTimeEpochMs() {
    return startTimeEpochMs;
  }

  public void setStartTimeEpochMs(long startTimeEpochMs) {
    this.startTimeEpochMs = startTimeEpochMs;
  }

  public long getEndTimeEpochMs() {
    return endTimeEpochMs;
  }

  public void setEndTimeEpochMs(long endTimeEpochMs) {
    this.endTimeEpochMs = endTimeEpochMs;
  }

  public AuctionStatus getStatus() {
    return status;
  }

  public void setStatus(AuctionStatus status) {
    this.status = status;
  }
}
