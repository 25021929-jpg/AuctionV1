package com.auction.shared.model;

import com.auction.enums.UserRole;

public class Admin extends User {
    public Admin() { super(); this.role = UserRole.ADMIN; }

    public Admin(Long id, String username, String email, String passwordHash) {
        super(id, username, email, passwordHash, UserRole.ADMIN);
    }

    @Override
    public void printInfo() {
        System.out.printf("[ADMIN] id=%d | %s | %s%n", id, username, email);
    }
}