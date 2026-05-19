package com.auction.client.feature.auth.validator;

import com.auction.client.feature.auth.dto.request.ResetPasswordRequest;
import com.auction.validation.FieldError;
import com.auction.validation.FieldValidator;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import com.auction.validation.rules.MatchRule;
import com.auction.validation.rules.MinLengthRule;
import com.auction.validation.rules.NotBlankRule;

import java.util.ArrayList;
import java.util.List;

public class ResetPasswordValidator implements Validator<ResetPasswordRequest> {

    @Override
    public ValidationResult validate(ResetPasswordRequest request) {
        List<FieldError> errors = new ArrayList<>();

        // Validate password
        new FieldValidator<>("password", request.password(),
                new NotBlankRule(),
                new MinLengthRule(8)
        ).validate().ifPresent(errors::add);

        // Validate confirmPassword — chỉ check nếu password đã hợp lệ
        if (errors.isEmpty()) {
            new FieldValidator<>("confirmPassword", request.confirmPassword(),
                    new MatchRule(request.password()) // ← cần tạo thêm rule này
            ).validate().ifPresent(errors::add);
        }

        return ValidationResult.from(errors);
    }
}
