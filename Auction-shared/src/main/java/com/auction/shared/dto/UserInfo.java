package com.auction.shared.dto;

import java.math.BigDecimal;

public class UserInfo {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String role;
    private BigDecimal balance = BigDecimal.ZERO;
    // ↑ KHÔNG có passwordHash — đây là lý do tách ra!

    // Bắt buộc có constructor rỗng cho Gson
    public UserInfo() {}

    public UserInfo(Long id, String fullName, String username,
                    String email, String phone,
                    String dateOfBirth, String role) {
        this(id, fullName, username, email, phone, dateOfBirth, role, BigDecimal.ZERO);
    }

    public UserInfo(Long id, String fullName, String username,
                    String email, String phone,
                    String dateOfBirth, String role, BigDecimal balance) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
        setBalance(balance);
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getRole() { return role; }
    public BigDecimal getBalance() { return balance == null ? BigDecimal.ZERO : balance; }

    public void setBalance(BigDecimal balance) {
        this.balance = balance == null ? BigDecimal.ZERO : balance;
    }
}
