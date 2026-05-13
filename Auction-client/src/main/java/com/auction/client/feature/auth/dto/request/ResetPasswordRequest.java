package com.auction.client.feature.auth.dto.request;

public record ResetPasswordRequest(String password, String confirmPassword) {}