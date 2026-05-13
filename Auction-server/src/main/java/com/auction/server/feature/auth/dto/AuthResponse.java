package com.auction.server.feature.auth.dto;

public class AuthResponse {

    private UserInfo user;

    public AuthResponse(UserInfo user) {
        this.user = user;
    }

    public static AuthResponse fromUserInfo(UserInfo userInfo) {
        return new AuthResponse(userInfo);
    }

    public UserInfo getUser() {
        return user;
    }
}