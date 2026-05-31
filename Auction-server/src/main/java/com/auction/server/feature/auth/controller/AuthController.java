package com.auction.server.feature.auth.controller;

import com.auction.server.feature.auth.AuthException;
import com.auction.server.feature.auth.service.AuthService;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.auth.request.LoginRequest;
import com.auction.shared.dto.auth.request.RegisterRequest;
import com.auction.shared.protocol.JsonSupport;
import com.google.gson.Gson;

public class AuthController {

    private final AuthService authService;
    private final Gson gson;
    // SỬ dụng Dependency INJECTION dễ mock test,...
    public AuthController(AuthService authService) {
        this.authService = authService;
        this.gson = JsonSupport.createGson();
    }

    public Response<com.auction.shared.dto.AuthResponse> register(String body) {
        try {
            RegisterRequest request = gson.fromJson(body, RegisterRequest.class);

            com.auction.shared.dto.AuthResponse result = authService.register(request);

            return Response.success("Register success", result);

        } catch (AuthException e) {
            return Response.fail(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return Response.fail("Internal server error");
        }
    }
}
