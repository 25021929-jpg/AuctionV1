package com.auction.model;

/**
 * Bidder - Người đặt giá / tham gia đấu giá
 * Implements AuctionObserver để nhận thông báo khi bị vượt giá
 * Implements AuctionStrategy để thực hiện hành động đặt giá
 *
 * PATTERN SỬ DỤNG:
 * - Observer: Bidder nhận thông báo khi có người đặt giá cao hơn
 * - Strategy: BidStrategy - chiến lược đặt giá của Bidder
 */
public class Bidder extends User implements AuctionObserver, AuctionStrategy {

    private double balance;        // Số dư tài khoản
    private double currentBid;     // Giá đang đặt hiện tại
    private int totalBidsPlaced;   // Tổng số lần đã đặt giá

    public Bidder(int id, String username, String password, String email,
                  String fullName, double balance) {
        super(id, username, password, email, fullName, Role.BIDDER);
        this.balance = balance;
        this.currentBid = 0;
        this.totalBidsPlaced = 0;
    }

    // ===== OBSERVER PATTERN =====
    @Override
    public void onPriceUpdated(String itemName, double newPrice, String bidderName) {
        if (!bidderName.equals(getUsername())) {
            System.out.printf("[THÔNG BÁO -> %s] Bạn đã bị vượt giá! Sản phẩm '%s' vừa được đặt %.0f VNĐ bởi %s%n",
                    getUsername(), itemName, newPrice, bidderName);
        }
    }

    @Override
    public void onAuctionEnded(String itemName, double finalPrice, String winnerName) {
        if (winnerName.equals(getUsername())) {
            System.out.printf("[CHÚC MỪNG -> %s] Bạn đã thắng phiên đấu giá '%s' với giá %.0f VNĐ!%n",
                    getUsername(), itemName, finalPrice);
            balance -= finalPrice;
        } else {
            System.out.printf("[THÔNG BÁO -> %s] Phiên đấu giá '%s' kết thúc. Người thắng: %s%n",
                    getUsername(), itemName, winnerName);
        }
    }

    // ===== STRATEGY PATTERN =====
    @Override
    public void performAction(String itemName, double bidPrice) {
        if (bidPrice > balance) {
            System.out.printf("[LỖI] %s không đủ số dư để đặt giá %.0f VNĐ (Số dư: %.0f VNĐ)%n",
                    getUsername(), bidPrice, balance);
            return;
        }
        if (bidPrice <= currentBid) {
            System.out.printf("[LỖI] Giá đặt phải cao hơn giá hiện tại (%.0f VNĐ)%n", currentBid);
            return;
        }
        currentBid = bidPrice;
        totalBidsPlaced++;
        System.out.printf("[ĐẶT GIÁ] %s đặt giá %.0f VNĐ cho sản phẩm '%s'%n",
                getUsername(), bidPrice, itemName);
    }

    @Override
    public String getStrategyDescription() {
        return "Bidder Strategy: Đặt giá trực tiếp vào phiên đấu giá";
    }

    // Getters & Setters
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getCurrentBid() { return currentBid; }
    public void setCurrentBid(double currentBid) { this.currentBid = currentBid; }

    public int getTotalBidsPlaced() { return totalBidsPlaced; }

    public void deposit(double amount) {
        balance += amount;
        System.out.printf("[NẠP TIỀN] %s nạp %.0f VNĐ. Số dư hiện tại: %.0f VNĐ%n",
                getUsername(), amount, balance);
    }

    @Override
    public String getUserInfo() {
        return String.format("BIDDER | ID: %d | Tên: %s | Số dư: %.0f VNĐ | Đã đặt: %d lần",
                getId(), getFullName(), balance, totalBidsPlaced);
    }
}