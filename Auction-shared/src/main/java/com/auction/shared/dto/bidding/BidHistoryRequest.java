package com.auction.shared.dto.bidding;

/** Request lấy lịch sử đặt giá của một phiên đấu giá. */
public class BidHistoryRequest {
    private long auctionId;
    private int limit;

    public BidHistoryRequest() {
    }

    public BidHistoryRequest(long auctionId, int limit) {
        this.auctionId = auctionId;
        this.limit = limit;
    }

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
