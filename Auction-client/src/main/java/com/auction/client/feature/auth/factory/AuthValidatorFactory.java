package com.auction.client.feature.auth.factory;

import com.auction.client.feature.auth.dto.request.ForgotPasswordRequest;
import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.auction.client.feature.auth.dto.request.OtpRequest;
import com.auction.client.feature.auth.dto.request.RegisterRequest;
import com.auction.client.feature.auth.dto.request.ResetPasswordRequest;
import com.auction.client.feature.auth.validator.ForgotPasswordValidator;
import com.auction.client.feature.auth.validator.LoginValidator;
import com.auction.client.feature.auth.validator.OtpValidator;
import com.auction.client.feature.auth.validator.RegisterValidator;
import com.auction.client.feature.auth.validator.ResetPasswordValidator;
import com.auction.validation.Validator;

public class AuthValidatorFactory {

    public static Validator<RegisterRequest> createRegisterValidator() {
        return new RegisterValidator();
    }

    public static Validator<LoginRequest> createLoginValidator() {
        return new LoginValidator();
    }

    public static Validator<ForgotPasswordRequest> createForgotPasswordValidator() {
        return new ForgotPasswordValidator();
    }

    public static Validator<OtpRequest> createOtpValidator() {
        return new OtpValidator();
    }

    public static Validator<ResetPasswordRequest> createResetPasswordValidator() {
        return new ResetPasswordValidator();
    }
}