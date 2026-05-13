package com.auction.shared.validation.rules;

import com.auction.shared.validation.ValidationRule;

import java.util.Optional;

public class EmailFormatRule implements ValidationRule<String> {

    // Giải thích REGEX:
    // ^              → bắt đầu chuỗi
    // [A-Za-z0-9+_.-] → phần trước @ — chữ, số, +, _, ., -
    // +              → 1 ký tự trở lên
    // @              → dấu @
    // [A-Za-z0-9.-]  → tên domain — chữ, số, ., -
    // +              → 1 ký tự trở lên
    // \.             → dấu chấm trước đuôi
    // [A-Za-z]{2,}   → đuôi domain tối thiểu 2 ký tự (com, vn, org...) (Bỏ)
    //com -> đuôi cuối phải là .com
    // $              → kết thúc chuỗi
    private static final String REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$";

    @Override
    public Optional<String> check(String value) {
        if (value == null || !value.trim().matches(REGEX)) {
            return Optional.of("Email không hợp lệ");
        }
        return Optional.empty();
    }
}
