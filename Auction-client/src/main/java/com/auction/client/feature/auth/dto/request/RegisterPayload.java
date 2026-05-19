package com.auction.client.feature.auth.dto.request;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegisterPayload {

    private String fullName;
    private String username;
    private String email;
    private String phone;        // khớp server
    private String dateOfBirth;  // khớp server
    private String password;
    private String confirmPassword;

    public RegisterPayload(RegisterRequest req) {
        this.fullName        = req.fullName();
        this.username        = req.username();
        this.email           = req.email();
        this.phone           = req.phoneNumber(); // map từ phoneNumber → phone
        this.password        = req.password();
        this.confirmPassword = req.confirmPassword();
        this.dateOfBirth     = req.birthDate() != null
                ? req.birthDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : null;
    }
}
