package com.auction.client.feature.seller.validator;

import com.auction.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellerItemFormValidatorTest {

    @Test
    void validInputPasses() {
        ValidationResult result = SellerItemFormValidator.validate(
                "Laptop đấu giá",
                "Máy còn tốt",
                "1000000",
                LocalDateTime.parse("2026-05-26T20:00:00"),
                LocalDateTime.parse("2026-05-27T20:00:00")
        );

        assertTrue(result.valid());
    }

    @Test
    void blankNameFails() {
        ValidationResult result = SellerItemFormValidator.validate(
                " ",
                "Mô tả",
                "1000000",
                null,
                LocalDateTime.parse("2026-05-27T20:00:00")
        );

        assertFalse(result.valid());
        assertTrue(result.hasErrorFor("name"));
    }

    @Test
    void nonPositivePriceFails() {
        ValidationResult result = SellerItemFormValidator.validate(
                "Laptop",
                "Mô tả",
                "0",
                null,
                LocalDateTime.parse("2026-05-27T20:00:00")
        );

        assertFalse(result.valid());
        assertTrue(result.hasErrorFor("startPrice"));
    }

    @Test
    void startAfterEndFails() {
        ValidationResult result = SellerItemFormValidator.validate(
                "Laptop",
                "Mô tả",
                "1000000",
                LocalDateTime.parse("2026-05-28T20:00:00"),
                LocalDateTime.parse("2026-05-27T20:00:00")
        );

        assertFalse(result.valid());
        assertTrue(result.hasErrorFor("endTime"));
    }
}
