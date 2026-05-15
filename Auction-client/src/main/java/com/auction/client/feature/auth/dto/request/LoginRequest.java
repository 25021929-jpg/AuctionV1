package com.auction.client.feature.auth.dto.request;

public record LoginRequest(
        String loginId, //Email hoặc tên đăng nhập
        String password

) {}
