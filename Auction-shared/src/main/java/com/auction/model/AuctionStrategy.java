package com.auction.model;

/**
 * STRATEGY PATTERN - Interface
 * Định nghĩa hành động mà mỗi loại User có thể thực hiện
 */
public interface AuctionStrategy {
    void performAction(String itemName, double price);
    String getStrategyDescription();
}