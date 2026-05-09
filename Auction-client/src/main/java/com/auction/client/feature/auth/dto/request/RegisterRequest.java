package com.auction.client.feature.auth.dto.request;

public record RegisterRequest(
        String username,
        String email,
        String phoneNumber,
        String password,
        String confirmPassword
) {}
