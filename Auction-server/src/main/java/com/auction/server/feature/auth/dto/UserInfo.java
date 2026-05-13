package com.auction.server.feature.auth.dto;

import com.auction.shared.model.User;

public class UserInfo {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String role;

    public UserInfo(
            Long id,
            String fullName,
            String username,
            String email,
            String phone,
            String dateOfBirth,
            String role
    ) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
    }

    // Convert User entity sang dữ liệu an toàn để trả về client
    public static UserInfo fromUser(User user) {
        return new UserInfo(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getDateOfBirth(),
                user.getRole()
        );
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getRole() { return role; }
}