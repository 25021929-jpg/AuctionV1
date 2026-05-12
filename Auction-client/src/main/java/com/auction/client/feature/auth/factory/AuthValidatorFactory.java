package com.auction.client.feature.auth.factory;

import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.auction.client.feature.auth.dto.request.RegisterRequest;
import com.auction.client.feature.auth.validator.LoginValidator;
import com.auction.client.feature.auth.validator.RegisterValidator;
import com.auction.validation.FieldValidator;
import com.auction.validation.Validator;

public class AuthValidatorFactory {
    public static Validator<LoginRequest> createloginValidator(){
        return new LoginValidator();

    }

    public static Validator<RegisterRequest> createRegisterValidator(){
        return new RegisterValidator();
    }
}
