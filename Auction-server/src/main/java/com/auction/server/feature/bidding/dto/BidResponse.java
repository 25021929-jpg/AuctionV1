package com.auction.server.feature.bidding.dto;

public class BidResponse {

    private int bidId;
    private int auctionSessionId;
    private int bidderId;
    private double bidAmount;
    private String message;

    public BidResponse() {
    }

    public BidResponse(int bidId, int auctionSessionId, int bidderId, double bidAmount, String message) {
        this.bidId = bidId;
        this.auctionSessionId = auctionSessionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.message = message;
    }

    public int getBidId() {
        return bidId;
    }

    public int getAuctionSessionId() {
        return auctionSessionId;
    }

    public int getBidderId() {
        return bidderId;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public String getMessage() {
        return message;
    }
}