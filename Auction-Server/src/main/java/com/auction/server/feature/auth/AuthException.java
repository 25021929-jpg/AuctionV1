package com.auction.server.feature.auth;

public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}