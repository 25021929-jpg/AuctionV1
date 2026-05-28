package com.auction.shared.model;

import java.io.Serializable;

/**
 * Shared bid transaction model (minimal).
 */
public class BidTransaction implements Serializable {
    private long id;
    private long auctionId;
    private long bidderId;
    private double amount;
    private long timestampEpochMs;

    public BidTransaction() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(long auctionId) {
        this.auctionId = auctionId;
    }

    public long getBidderId() {
        return bidderId;
    }

    public void setBidderId(long bidderId) {
        this.bidderId = bidderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestampEpochMs() {
        return timestampEpochMs;
    }

    public void setTimestampEpochMs(long timestampEpochMs) {
        this.timestampEpochMs = timestampEpochMs;
    }
}
