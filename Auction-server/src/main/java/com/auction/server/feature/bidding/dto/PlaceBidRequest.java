package com.auction.server.feature.bidding.dto;

public class PlaceBidRequest {

    private int auctionSessionId;
    private int bidderId;
    private double bidAmount;

    public PlaceBidRequest() {
    }

    public PlaceBidRequest(int auctionSessionId, int bidderId, double bidAmount) {
        this.auctionSessionId = auctionSessionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
    }

    public int getAuctionSessionId() {
        return auctionSessionId;
    }

    public void setAuctionSessionId(int auctionSessionId) {
        this.auctionSessionId = auctionSessionId;
    }

    public int getBidderId() {
        return bidderId;
    }

    public void setBidderId(int bidderId) {
        this.bidderId = bidderId;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }
}