package com.auction.server.feature.auth.dto;

// DTO dùng để nhận dữ liệu đăng nhập từ client
public class LoginRequest {

    private String username; // Tên đăng nhập
    private String password; // Mật khẩu

    // Constructor rỗng: cần cho JSON convert
    public LoginRequest() {
    }

    // Constructor đầy đủ
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter / Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}