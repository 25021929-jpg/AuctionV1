package com.auction.model.entity;

import com.auction.enums.UserRole;

public class Bidder extends User {
    public Bidder() { super(); this.role = UserRole.BIDDER; }

    public Bidder(Long id, String username, String email, String passwordHash) {
        super(id, username, email, passwordHash, UserRole.BIDDER);
    }

    public boolean canBid() { return isActive; }

    @Override
    public void printInfo() {
        System.out.printf("[BIDDER] id=%d | %s | %s%n", id, username, email);
    }
}