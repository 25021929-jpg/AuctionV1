package com.auction.server.network;

import com.auction.server.feature.auth.controller.AuthController;
import com.auction.shared.dto.Request;
import com.auction.shared.dto.Response;

public class RequestDispatcher {

    private final AuthController authController;

    public RequestDispatcher() {
        this.authController = new AuthController();
    }

    public Response<?> dispatch(Request request) {

        // Kiểm tra request có null không
        if (request == null) {
            return Response.fail("Request is null");
        }

        // Kiểm tra action có null/rỗng không
        if (request.getAction() == null || request.getAction().trim().isEmpty()) {
            return Response.fail("Action is required");
        }

        String action = request.getAction().trim();

        // Dựa vào action để gọi đúng controller
        switch (action) {

            case "AUTH_REGISTER":
                return authController.register(request.getBody());

            case "AUTH_LOGIN":
                return authController.login(request.getBody());

            case "AUTH_FORGOT_PASSWORD":
                return authController.forgotPassword(request.getBody());

            case "AUTH_RESET_PASSWORD":
                return authController.resetPassword(request.getBody());

            default:
                return Response.fail("Unsupported action: " + action);
        }
    }
}