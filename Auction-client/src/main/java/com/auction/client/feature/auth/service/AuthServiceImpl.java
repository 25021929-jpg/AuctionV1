package com.auction.client.feature.auth.service;

import com.auction.shared.protocol.ActionConstants;
import com.auction.client.core.error.ApiException;
import com.auction.client.core.error.ResponseUtils;
import com.auction.client.network.ServerCommunicator;
import com.auction.client.network.SocketClient;
import com.auction.shared.dto.AuthResponse;
import com.auction.shared.dto.Response;
import com.auction.shared.dto.auth.request.LoginRequest;
import com.auction.shared.dto.auth.request.RegisterRequest;

import java.io.IOException;

/**
 * AuthService implementation.
 *
 * <p>Controller chỉ điều khiển UI; service chịu trách nhiệm gọi server và xử lý response.
 */
public class AuthServiceImpl implements AuthService {

    private final ServerCommunicator communicator;

    public AuthServiceImpl() {
        this(SocketClient.getInstance());
    }

    public AuthServiceImpl(ServerCommunicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public AuthResponse login(LoginRequest request) throws IOException {
        Response<AuthResponse> response = communicator.send(
                ActionConstants.AUTH_LOGIN,
                request,
                AuthResponse.class
        );
        AuthResponse authResponse = ResponseUtils.unwrap(ActionConstants.AUTH_LOGIN, response);
        if (authResponse == null || authResponse.getUser() == null) {
            throw new ApiException(ActionConstants.AUTH_LOGIN,
                    "Server xác nhận đăng nhập nhưng không trả thông tin người dùng.");
        }
        return authResponse;
    }

    @Override
    public void register(RegisterRequest request) throws IOException {
        Response<Void> response = communicator.send(
                ActionConstants.AUTH_REGISTER,
                request,
                Void.class
        );
        ResponseUtils.unwrap(ActionConstants.AUTH_REGISTER, response);
    }
}
