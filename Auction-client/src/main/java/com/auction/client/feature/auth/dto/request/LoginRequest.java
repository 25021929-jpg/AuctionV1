package com.auction.client.feature.auth.dto.request;

public record LoginRequest(
        String identity, //Email hoặc tên đăng nhập
        String password

) {}
