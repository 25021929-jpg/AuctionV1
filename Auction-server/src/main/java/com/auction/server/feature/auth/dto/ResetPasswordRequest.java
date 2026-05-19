package com.auction.server.feature.auth.dto;

public class ResetPasswordRequest {

    private String token;
    private String newPassword;
    private String confirmPassword;

    public ResetPasswordRequest() {}

    public String getToken() { return token; }
    public String getNewPassword() { return newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
}