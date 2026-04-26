package com.auction.model;

/**
 * Base class cho tất cả người dùng trong hệ thống
 */
public abstract class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private Role role;

    public User(int id, String username, String password, String email, String fullName, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    // Abstract method - mỗi loại User tự mô tả mình
    public abstract String getUserInfo();

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role=%s}", id, username, role);
    }
}