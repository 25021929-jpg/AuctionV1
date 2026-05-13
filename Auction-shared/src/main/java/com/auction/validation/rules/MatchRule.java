package com.auction.validation.rules;

import com.auction.validation.ValidationRule;

import java.util.Optional;

public class MatchRule implements ValidationRule<String> {

    private final String target;

    public MatchRule(String target) {
        this.target = target;
    }

    @Override
    public Optional<String> check(String value) {
        if (value == null || !value.equals(target)) {
            return Optional.of("Mật khẩu xác nhận không khớp");
        }
        return Optional.empty();
    }
}
