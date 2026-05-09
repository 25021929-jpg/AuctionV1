package com.auction.client.feature.auth.dto.request;

public record LoginRequest(
        String username,
        String password

) {}
