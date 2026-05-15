package com.auction.client.feature.auth.dto.request;

import java.time.LocalDate;

public record RegisterRequest(
        String fullName,
        String username,
        String email,
        String phone,
        String dateOfBirth,
        String password,
        String confirmPassword
) {}