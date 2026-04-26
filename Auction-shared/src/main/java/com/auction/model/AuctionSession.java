package com.auction.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Phiên đấu giá - lớp trung tâm kết hợp cả 3 design patterns
 *
 * PATTERN SỬ DỤNG:
 * - Observer: Quản lý danh sách observers và notify khi có sự kiện
 * - Strategy: Gọi performAction() của từng User theo vai trò
 * - Factory Method: Dùng UserFactory để tạo User
 */
public class AuctionSession {

    private String itemName;
    private double currentPrice;
    private double startingPrice;
    private String highestBidder;
    private boolean isActive;
    private List<AuctionObserver> observers; // OBSERVER PATTERN - danh sách người theo dõi

    public AuctionSession(String itemName, double startingPrice) {
        this.itemName = itemName;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.highestBidder = "Chưa có";
        this.isActive = true;
        this.observers = new ArrayList<>();
        System.out.println("\n===== PHIÊN ĐẤU GIÁ BẮT ĐẦU =====");
        System.out.printf("Sản phẩm: %s | Giá khởi điểm: %.0f VNĐ%n%n", itemName, startingPrice);
    }

    // ===== OBSERVER PATTERN - Đăng ký/Hủy theo dõi =====
    public void addObserver(AuctionObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    private void notifyPriceUpdated(double newPrice, String bidderName) {
        for (AuctionObserver observer : observers) {
            observer.onPriceUpdated(itemName, newPrice, bidderName);
        }
    }

    private void notifyAuctionEnded(double finalPrice, String winnerName) {
        for (AuctionObserver observer : observers) {
            observer.onAuctionEnded(itemName, finalPrice, winnerName);
        }
    }

    // ===== STRATEGY PATTERN - Xử lý đặt giá =====
    public void placeBid(Bidder bidder, double bidPrice) {
        if (!isActive) {
            System.out.println("[LỖI] Phiên đấu giá đã kết thúc!");
            return;
        }
        if (bidPrice <= currentPrice) {
            System.out.printf("[LỖI] Giá đặt phải cao hơn giá hiện tại: %.0f VNĐ%n", currentPrice);
            return;
        }

        // Gọi Strategy của Bidder
        bidder.setCurrentBid(bidPrice);
        bidder.performAction(itemName, bidPrice); // STRATEGY PATTERN

        currentPrice = bidPrice;
        highestBidder = bidder.getUsername();

        // Thông báo tới tất cả observers
        notifyPriceUpdated(currentPrice, highestBidder); // OBSERVER PATTERN
    }

    public void listItem(Seller seller, double price) {
        // Gọi Strategy của Seller
        seller.performAction(itemName, price); // STRATEGY PATTERN
    }

    public void endAuction() {
        if (!isActive) return;
        isActive = false;
        System.out.println("\n===== KẾT THÚC PHIÊN ĐẤU GIÁ =====");
        System.out.printf("Sản phẩm: %s | Giá cuối: %.0f VNĐ | Người thắng: %s%n%n",
                itemName, currentPrice, highestBidder);
        notifyAuctionEnded(currentPrice, highestBidder); // OBSERVER PATTERN
    }

    // Getters
    public String getItemName() { return itemName; }
    public double getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }
    public boolean isActive() { return isActive; }
}