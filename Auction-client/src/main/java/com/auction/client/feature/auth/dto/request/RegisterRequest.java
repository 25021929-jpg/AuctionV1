package com.auction.client.feature.auth.dto.request;

import java.time.LocalDate;

public record RegisterRequest(
        String fullName,
        String username,
        String email,
        String phoneNumber,
        String password,
        String confirmPassword,
        LocalDate birthDate
) {}
