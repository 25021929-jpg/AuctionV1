package com.auction.shared.model;

import com.auction.enums.UserRole;

public class Seller extends User {
    private String shopName;

    public Seller() { super(); this.role = UserRole.SELLER; }

    public Seller(Long id, String username, String email,
                  String passwordHash, String shopName) {
        super(id, username, email, passwordHash, UserRole.SELLER);
        this.shopName = shopName;
    }

    public boolean canCreateAuction() { return isActive; }
    public String getShopName() { return shopName; }
    public void setShopName(String s) { this.shopName = s; }

    @Override
    public void printInfo() {
        System.out.printf("[SELLER] id=%d | %s | shop=%s%n", id, username, shopName);
    }
}