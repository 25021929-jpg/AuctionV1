package com.auction.shared.validation.rules;

import com.auction.shared.validation.ValidationRule;
import java.util.Optional;

public class FullNameRule implements ValidationRule<String> {

    // Giải thích REGEX:
    // \p{L}  → mọi chữ cái Unicode — tiếng Việt, Anh, Nhật... đều được
    // \s     → khoảng trắng
    // +      → 1 ký tự trở lên
    // [ ]    → chỉ cho phép chữ cái và khoảng trắng, không có gì khác
    private static final String REGEX = "^[\\p{L}\\s]+$";

    @Override
    public Optional<String> check(String value) {
        if (!value.matches(REGEX)) {
            return Optional.of("Tên chỉ được chứa chữ cái");
        }
        return Optional.empty();
    }
}