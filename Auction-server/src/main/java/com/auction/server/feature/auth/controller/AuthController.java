package com.auction.server.feature.auth.controller;

import com.auction.server.feature.auth.AuthException;
import com.auction.server.feature.auth.dto.*;
import com.auction.server.feature.auth.service.AuthService;
import com.auction.shared.dto.Response;
import com.google.gson.Gson;

public class AuthController {

    private final AuthService authService;
    private final Gson gson;

    public AuthController() {
        this.authService = new AuthService();
        this.gson = new Gson();
    }

    public Response<com.auction.shared.dto.AuthResponse> register(String body) {
        try {
            RegisterRequest request = gson.fromJson(body, RegisterRequest.class);

            com.auction.shared.dto.AuthResponse result = authService.register(request);

            return Response.success("Register success", result);

        } catch (AuthException e) {
            return Response.fail(e.getMessage());

        } catch (Exception e) {
            return Response.fail("Internal server error");
        }
    }

    public Response<com.auction.shared.dto.AuthResponse> login(String body) {
        try {
            LoginRequest request = gson.fromJson(body, LoginRequest.class);

            com.auction.shared.dto.AuthResponse result = authService.login(request);

            return Response.success("Login success", result);

        } catch (AuthException e) {
            return Response.fail(e.getMessage());

        } catch (Exception e) {
            return Response.fail("Internal server error");
        }
    }

    public Response<String> forgotPassword(String body) {
        try {
            ForgotPasswordRequest request = gson.fromJson(body, ForgotPasswordRequest.class);

            String resetToken = authService.forgotPassword(request);

            // Tạm thời trả token để test.
            // Sau này có email thật thì không trả token trực tiếp nữa.
            return Response.success("Reset token created", resetToken);

        } catch (AuthException e) {
            return Response.fail(e.getMessage());

        } catch (Exception e) {
            return Response.fail("Internal server error");
        }
    }

    public Response<String> resetPassword(String body) {
        try {
            ResetPasswordRequest request = gson.fromJson(body, ResetPasswordRequest.class);

            authService.resetPassword(request);

            return Response.success("Password reset success", null);

        } catch (AuthException e) {
            return Response.fail(e.getMessage());

        } catch (Exception e) {
            return Response.fail("Internal server error");
        }
    }
}