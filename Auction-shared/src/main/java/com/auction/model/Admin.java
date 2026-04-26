package com.auction.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin - Quản trị viên hệ thống
 * Implements AuctionObserver để theo dõi mọi phiên đấu giá
 *
 * PATTERN SỬ DỤNG:
 * - Observer: Admin luôn được thông báo khi có sự kiện đấu giá
 * - Strategy: Admin có quyền hủy/dừng phiên đấu giá (AdminStrategy)
 */
public class Admin extends User implements AuctionObserver {

    private List<String> activityLog; // Lưu lịch sử hoạt động

    public Admin(int id, String username, String password, String email, String fullName) {
        super(id, username, password, email, fullName, Role.ADMIN);
        this.activityLog = new ArrayList<>();
    }

    // ===== OBSERVER PATTERN =====
    @Override
    public void onPriceUpdated(String itemName, double newPrice, String bidderName) {
        String log = String.format("[ADMIN LOG] Giá mới cho '%s': %.0f VNĐ - Người đặt: %s",
                itemName, newPrice, bidderName);
        activityLog.add(log);
        System.out.println(log);
    }

    @Override
    public void onAuctionEnded(String itemName, double finalPrice, String winnerName) {
        String log = String.format("[ADMIN LOG] Phiên đấu giá '%s' kết thúc - Giá cuối: %.0f VNĐ - Người thắng: %s",
                itemName, finalPrice, winnerName);
        activityLog.add(log);
        System.out.println(log);
    }

    // ===== ADMIN ACTIONS =====
    public void approveAuction(String itemName) {
        System.out.println("[ADMIN] " + getFullName() + " đã duyệt phiên đấu giá: " + itemName);
    }

    public void cancelAuction(String itemName) {
        System.out.println("[ADMIN] " + getFullName() + " đã hủy phiên đấu giá: " + itemName);
    }

    public void banUser(String username) {
        System.out.println("[ADMIN] " + getFullName() + " đã khóa tài khoản: " + username);
    }

    public List<String> getActivityLog() {
        return activityLog;
    }

    @Override
    public String getUserInfo() {
        return String.format("ADMIN | ID: %d | Tên: %s | Email: %s",
                getId(), getFullName(), getEmail());
    }
}