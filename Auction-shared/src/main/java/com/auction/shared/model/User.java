package com.auction.shared.model;

public class User {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String dateOfBirth; // yyyy-MM-dd
    private String passwordHash;
    private String role;

    // Constructor rỗng để Gson / mapping có thể dùng
    public User() {
    }

    // Constructor đầy đủ để Repository tạo User từ database
    public User(
            Long id,
            String fullName,
            String username,
            String email,
            String phone,
            String dateOfBirth,
            String passwordHash,
            String role
    ) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(String role) {
        this.role = role;
    }
}