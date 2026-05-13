package com.auction.validation.rules;

import com.auction.validation.ValidationRule;
import java.util.Optional;

public class EmailOrUsernameRule implements ValidationRule<String> {

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public Optional<String> check(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty(); // NotBlankRule đã xử lý
        }

        // Có "@" → kiểm tra format email
        if (value.contains("@")) {
            if (!value.trim().matches(EMAIL_REGEX)) {
                return Optional.of("Sai định dạng email");
            }
        }
        // Không có "@" → là username → không check gì thêm

        return Optional.empty();
    }
}