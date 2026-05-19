package com.auction.client.feature.auth.validator;

import com.auction.client.feature.auth.dto.request.OtpRequest;
import com.auction.validation.FieldError;
import com.auction.validation.FieldValidator;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import com.auction.validation.rules.NotBlankRule;
import com.auction.validation.rules.OtpFormatRule;

import java.util.ArrayList;
import java.util.List;

public class OtpValidator implements Validator<OtpRequest> {

    @Override
    public ValidationResult validate(OtpRequest request) {
        List<FieldError> errors = new ArrayList<>();

        new FieldValidator<>("otp", request.otp(),
                new NotBlankRule(),
                new OtpFormatRule()
        ).validate().ifPresent(errors::add);

        return ValidationResult.from(errors);
    }
}