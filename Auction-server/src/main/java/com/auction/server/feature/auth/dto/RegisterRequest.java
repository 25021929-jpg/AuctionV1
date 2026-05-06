package com.auction.server.feature.auth.dto;

// DTO dùng để nhận dữ liệu đăng ký từ client
public class RegisterRequest {

    private String username; // Tên đăng nhập
    private String password; // Mật khẩu
    private String fullName; // Họ tên người dùng

    // Constructor rỗng: cần cho JSON convert
    public RegisterRequest() {
    }

    // Constructor đầy đủ
    public RegisterRequest(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}