package com.auction.shared.dto.auth.request;

/** Request đăng nhập dùng chung giữa client và server. */
public record LoginRequest(String identity, String password) {
}
