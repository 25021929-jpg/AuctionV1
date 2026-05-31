package com.auction.client.core.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Utility định dạng tiền dùng chung phía client. */
public final class MoneyFormat {

    private static final Locale VIETNAM = Locale.forLanguageTag("vi-VN");

    private MoneyFormat() {
    }

    public static String plain(BigDecimal amount) {
        return amount == null ? "-" : amount.stripTrailingZeros().toPlainString();
    }

    public static String grouped(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(VIETNAM);
        formatter.setMaximumFractionDigits(Math.max(0, amount.scale()));
        formatter.setMinimumFractionDigits(0);
        return formatter.format(amount);
    }

    public static BigDecimal parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Giá không được để trống");
        }

        String normalized = raw.trim()
                .replace(" ", "")
                .replace("_", "");

        // Cho phép người dùng nhập 1.000.000, 1.000.000,50, 1000000.50 hoặc 1000000,50.
        if (normalized.contains(",") && normalized.contains(".")) {
            normalized = normalized.replace(".", "").replace(',', '.');
        } else if (normalized.contains(",")) {
            normalized = normalized.replace(',', '.');
        } else if (normalized.indexOf('.') != normalized.lastIndexOf('.')) {
            normalized = normalized.replace(".", "");
        }

        return new BigDecimal(normalized);
    }
}
