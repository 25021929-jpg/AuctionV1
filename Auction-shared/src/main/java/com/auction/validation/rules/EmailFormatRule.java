package com.auction.validation.rules;

import com.auction.validation.ValidationRule;

import java.util.Optional;

public class EmailFormatRule implements ValidationRule<String> {

    /**
     * Regex email thực dụng cho UI: cho phép nhiều đuôi tên miền (.com, .vn, .edu.vn...).
     * Server/database vẫn có thể kiểm tra thêm nếu cần.
     */
    private static final String REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public Optional<String> check(String value) {
        if (value == null || !value.trim().matches(REGEX)) {
            return Optional.of("Email không hợp lệ");
        }
        return Optional.empty();
    }
}
