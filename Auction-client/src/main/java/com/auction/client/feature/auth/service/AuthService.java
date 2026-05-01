package com.auction.client.feature.auth.service;

import com.auction.client.feature.auth.dto.AuthResponse;

public interface AuthService {
    public AuthResponse login(String username, String password);
}
