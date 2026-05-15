package com.auction.server.feature.auth.dto;

public record LoginRequest(
        String identity, //Email hoặc tên đăng nhập
        String password

) {}