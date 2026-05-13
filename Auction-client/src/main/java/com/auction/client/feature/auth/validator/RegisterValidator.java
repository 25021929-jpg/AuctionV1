package com.auction.client.feature.auth.validator;
import com.auction.client.feature.auth.dto.request.RegisterRequest;
import com.auction.validation.*;
import com.auction.validation.rules.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegisterValidator implements Validator<RegisterRequest> {

    @Override
    public ValidationResult validate(RegisterRequest req) {
        List<FieldError> errors = new ArrayList<>();
        validate("fullName", req.fullName(),
                new NotBlankRule(),
                new FullNameRule())
                .ifPresent(errors::add);
//Lưu ý errors:: add = error -> errors.add (viết gọn hơn)
        //Mục đích là lưu các FieldError (value của Optional vào trong list errors)
        validate("username", req.username(),
                new NotBlankRule(),
                new MinLengthRule(3),
                new MaxLengthRule(20))
                .ifPresent(error -> errors.add(error));

        validate("password", req.password(),
                new NotBlankRule(),
                new MinLengthRule(8))
                .ifPresent(errors::add);

        validate("email", req.email(),
                new NotBlankRule(),
                new EmailFormatRule())
                .ifPresent(errors::add);

        validate("phoneNumber", req.phoneNumber(),
                new NotBlankRule(),
                new PhoneNumberRule())
                .ifPresent(errors::add);

        validate("birthDate", req.birthDate(),
                new MinAgeRule(18))
                .ifPresent(errors::add);
        //Lưu ý: reference method :: tương tự với lambda method parameter -> ....)
        // Rule đặc biệt — so sánh 2 field với nhau

        // Chỉ check password trùng với confirmpassword khi password đã đúng
        if (!errors.stream().anyMatch(e ->
                e.field().equals("password"))) {
            if (!req.password().equals(req.confirmPassword())) {
                errors.add(new FieldError(
                        "confirmPassword",
                        "Mật khẩu không khớp"
                ));
            }
        }

        if (errors.isEmpty()){
            return ValidationResult.ok();
        }
        else{
            return ValidationResult.from(errors);
        }
    }
    @SafeVarargs
    //Sử dụng extract method để viết một hàm helper riêng cho RegisterValidator
    private <T> Optional<FieldError> validate(String fieldName, T value, ValidationRule<T>... rules){
        return new FieldValidator<T>(fieldName,value,rules).validate();
    }

}
