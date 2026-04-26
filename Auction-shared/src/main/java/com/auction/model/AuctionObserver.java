package com.auction.model;

/**
 * OBSERVER PATTERN - Interface
 * Các User muốn nhận thông báo đấu giá phải implement interface này
 */
public interface AuctionObserver {
    void onPriceUpdated(String itemName, double newPrice, String bidderName);
    void onAuctionEnded(String itemName, double finalPrice, String winnerName);
}