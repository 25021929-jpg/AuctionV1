package com.auction.client.feature.auth.validator;

import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.auction.validation.*;
import com.auction.validation.rules.EmailOrUsernameRule;
import com.auction.validation.rules.MinLengthRule;
import com.auction.validation.rules.NotBlankRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoginValidator implements Validator<LoginRequest> {

    @Override
    public ValidationResult validate(LoginRequest req) {
        List<FieldError> errors = new ArrayList<>();

        validate("identity", req.identity(),
                new NotBlankRule(),
                new EmailOrUsernameRule())  // ← tự nhận biết
                .ifPresent(error -> errors.add(error));

        validate("password", req.password(),
                new NotBlankRule())
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
