package com.auction.server.feature.auth.dto;

public class LoginRequest {

    // loginId có thể là username hoặc email
    private String loginId;
    private String password;

    public LoginRequest() {}

    public String getLoginId() { return loginId; }
    public String getPassword() { return password; }
}