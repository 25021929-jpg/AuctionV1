package com.auction.shared.dto;


public class UserInfo {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String role;
    // ↑ KHÔNG có passwordHash — đây là lý do tách ra!

    // Bắt buộc có constructor rỗng cho Gson
    public UserInfo() {}

    public UserInfo(Long id, String fullName, String username,
                    String email, String phone,
                    String dateOfBirth, String role) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
    }


    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getRole() { return role; }
}