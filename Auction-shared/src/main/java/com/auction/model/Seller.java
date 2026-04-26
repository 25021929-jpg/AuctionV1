package com.auction.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Seller - Người bán / đăng sản phẩm đấu giá
 * Implements AuctionObserver để theo dõi phiên đấu giá của mình
 * Implements AuctionStrategy để thực hiện hành động đăng bán
 *
 * PATTERN SỬ DỤNG:
 * - Observer: Seller theo dõi giá thay đổi của sản phẩm mình đăng
 * - Strategy: SellerStrategy - chiến lược đăng bán của Seller
 */
public class Seller extends User implements AuctionObserver, AuctionStrategy {

    private List<String> listedItems;  // Danh sách sản phẩm đã đăng
    private double totalRevenue;       // Tổng doanh thu
    private double rating;             // Điểm đánh giá (1-5)

    public Seller(int id, String username, String password, String email,
                  String fullName, double rating) {
        super(id, username, password, email, fullName, Role.SELLER);
        this.listedItems = new ArrayList<>();
        this.totalRevenue = 0;
        this.rating = rating;
    }

    // ===== OBSERVER PATTERN =====
    @Override
    public void onPriceUpdated(String itemName, double newPrice, String bidderName) {
        if (listedItems.contains(itemName)) {
            System.out.printf("[SELLER THEO DÕI -> %s] Sản phẩm '%s' vừa được đặt giá %.0f VNĐ bởi %s%n",
                    getUsername(), itemName, newPrice, bidderName);
        }
    }

    @Override
    public void onAuctionEnded(String itemName, double finalPrice, String winnerName) {
        if (listedItems.contains(itemName)) {
            totalRevenue += finalPrice;
            System.out.printf("[SELLER -> %s] Sản phẩm '%s' đã bán được với giá %.0f VNĐ cho %s. Tổng doanh thu: %.0f VNĐ%n",
                    getUsername(), itemName, finalPrice, winnerName, totalRevenue);
        }
    }

    // ===== STRATEGY PATTERN =====
    @Override
    public void performAction(String itemName, double startingPrice) {
        listedItems.add(itemName);
        System.out.printf("[ĐĂNG BÁN] %s đã đăng sản phẩm '%s' với giá khởi điểm %.0f VNĐ%n",
                getUsername(), itemName, startingPrice);
    }

    @Override
    public String getStrategyDescription() {
        return "Seller Strategy: Đăng sản phẩm lên phiên đấu giá với giá khởi điểm";
    }

    // ===== SELLER ACTIONS =====
    public void removeItem(String itemName) {
        if (listedItems.remove(itemName)) {
            System.out.printf("[GỠ BÁN] %s đã gỡ sản phẩm '%s'%n", getUsername(), itemName);
        }
    }

    // Getters & Setters
    public List<String> getListedItems() { return listedItems; }

    public double getTotalRevenue() { return totalRevenue; }

    public double getRating() { return rating; }
    public void setRating(double rating) {
        if (rating >= 1 && rating <= 5) this.rating = rating;
    }

    @Override
    public String getUserInfo() {
        return String.format("SELLER | ID: %d | Tên: %s | Sản phẩm đăng: %d | Doanh thu: %.0f VNĐ | Rating: %.1f⭐",
                getId(), getFullName(), listedItems.size(), totalRevenue, rating);
    }
}