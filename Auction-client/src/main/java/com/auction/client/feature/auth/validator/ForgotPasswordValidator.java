package com.auction.client.feature.auth.validator;

import com.auction.client.feature.auth.dto.request.ForgotPasswordRequest;
import com.auction.validation.*;
import com.auction.validation.rules.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ForgotPasswordValidator implements Validator<ForgotPasswordRequest> {

    @Override
    public ValidationResult validate(ForgotPasswordRequest req) {
        List<FieldError> errors = new ArrayList<>();

        validate("email", req.email(),
                new NotBlankRule(),
                new EmailFormatRule())
                .ifPresent(errors::add);

        return errors.isEmpty()
                ? ValidationResult.ok()
                : ValidationResult.from(errors);
    }
    @SafeVarargs
    //Helper method validate để giúp cho validate các thuộc tính thuận tiện hơn
    private  static <T> Optional<FieldError> validate(String fieldName, T value, ValidationRule<T>... rules){
        return new FieldValidator<T>(fieldName, value, rules).validate();
    }
}