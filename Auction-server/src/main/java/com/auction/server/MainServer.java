package com.auction.server;

import com.auction.server.feature.auth.controller.AuthController;
import com.auction.server.feature.auth.dto.AuthResponse;
import com.auction.server.feature.auth.dto.LoginRequest;
import com.auction.server.feature.auth.dto.RegisterRequest;

public class MainServer {

    public static void main(String[] args) {
        AuthController authController = new AuthController();

        // Test đăng ký
        RegisterRequest registerRequest = new RegisterRequest(
                "hoang01",
                "123456",
                "Vu Hoang"
        );

        AuthResponse registerResponse = authController.register(registerRequest);

        System.out.println("REGISTER:");
        System.out.println(registerResponse.isSuccess());
        System.out.println(registerResponse.getMessage());

        // Test đăng nhập
        LoginRequest loginRequest = new LoginRequest(
                "hoang01",
                "123456"
        );

        AuthResponse loginResponse = authController.login(loginRequest);

        System.out.println("LOGIN:");
        System.out.println(loginResponse.isSuccess());
        System.out.println(loginResponse.getMessage());

        if (loginResponse.getUser() != null) {
            System.out.println(loginResponse.getUser().getUsername());
            System.out.println(loginResponse.getUser().getFullName());
            System.out.println(loginResponse.getUser().getRole());
        }
    }
}