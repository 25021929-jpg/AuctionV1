package com.auction.validation.rules;

import com.auction.validation.ValidationRule;

import java.util.Optional;

public class NotBlankRule implements ValidationRule<String>{
    @Override
    public Optional<String> check(String Value) {
        return (Value != null && !Value.trim().isEmpty())
                ? Optional.empty()
                : Optional.of("Thành phần không được để trống");
    }
}
