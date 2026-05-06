package com.auction.server.feature.auth.controller;

import com.auction.server.feature.auth.AuthException;
import com.auction.server.feature.auth.dto.AuthResponse;
import com.auction.server.feature.auth.dto.LoginRequest;
import com.auction.server.feature.auth.dto.RegisterRequest;
import com.auction.server.feature.auth.service.AuthService;

// Controller nhận request từ client rồi gọi service xử lý
public class AuthController {

    private final AuthService authService;

    // Constructor khởi tạo service
    public AuthController() {
        this.authService = new AuthService();
    }

    // API xử lý đăng ký
    public AuthResponse register(RegisterRequest request) {
        try {
            return authService.register(request);

        } catch (AuthException e) {
            return AuthResponse.fail(e.getMessage());
        }
    }

    // API xử lý đăng nhập
    public AuthResponse login(LoginRequest request) {
        try {
            return authService.login(request);

        } catch (AuthException e) {
            return AuthResponse.fail(e.getMessage());
        }
    }
}