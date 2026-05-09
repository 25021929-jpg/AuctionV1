package com.auction.validation.rules;

import com.auction.validation.ValidationRule;

import java.util.Optional;

public class EmailFormatRule implements ValidationRule<String> {
    private static final String REGEX = "^(?!.*\\\\.\\\\.)[A-Za-z0-9+_-]+@gmail\\.com$";
    @Override
    public Optional<String> check(String value) {
        return (value != null && value.matches(REGEX))
                ? Optional.empty()
                : Optional.of("Email không đúng định dạng");
    }
}
