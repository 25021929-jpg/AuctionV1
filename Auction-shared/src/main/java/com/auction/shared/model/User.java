package com.auction.model;

import java.io.Serializable;

public abstract class User implements Serializable {
    private String username;
    private String password;
    private String fullName;
    private Role role;

    // Constructor (Hàm khởi tạo)
    public User(String username, String password, String fullName, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // Các hàm Getter/Setter để lấy và sửa dữ liệu (Encapsulation - Đóng gói)
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
