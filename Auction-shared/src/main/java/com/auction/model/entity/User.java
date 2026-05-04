package com.auction.model.entity;

import com.auction.enums.UserRole;

public abstract class User extends Entity {
    protected String username;
    protected String email;
    protected String passwordHash;
    protected UserRole role;
    protected boolean isActive;

    protected User() { super(); this.isActive = true; }

    protected User(Long id, String username, String email,
                   String passwordHash, UserRole role) {
        super(id);
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
    }

    public boolean canLogin() { return isActive; }

    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String p) { this.passwordHash = p; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole r) { this.role = r; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean a) { this.isActive = a; }

    @Override
    public void printInfo() {
        System.out.printf("[%s] id=%d | %s | %s%n", role, id, username, email);
    }
}
