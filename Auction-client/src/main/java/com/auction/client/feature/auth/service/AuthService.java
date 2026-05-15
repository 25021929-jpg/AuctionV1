package com.auction.client.feature.auth.service;

import com.auction.client.network.SocketClient;
import com.auction.shared.dto.Request;
import com.auction.shared.dto.Response;
import com.google.gson.Gson;

public class AuthService {

    private final SocketClient socketClient = new SocketClient();
    private final Gson gson = new Gson();

    public AuthService() {
        socketClient.connect();
    }

    public Response<?> login(Object loginRequest) {
        Request request = new Request("AUTH_LOGIN", gson.toJson(loginRequest));
        String responseJson = socketClient.sendRequest(gson.toJson(request));
        return gson.fromJson(responseJson, Response.class);
    }

    public Response<?> register(Object registerRequest) {
        Request request = new Request("AUTH_REGISTER", gson.toJson(registerRequest));
        String responseJson = socketClient.sendRequest(gson.toJson(request));
        return gson.fromJson(responseJson, Response.class);
    }
}