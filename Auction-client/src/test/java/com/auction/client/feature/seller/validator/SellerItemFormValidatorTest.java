package com.auction.client.feature.seller.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.validation.ValidationResult;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SellerItemFormValidatorTest {

  @Test
  void validInputPasses() {
    LocalDateTime startTime = LocalDateTime.now().plusDays(1);
    LocalDateTime endTime = startTime.plusHours(2);

    ValidationResult result =
        SellerItemFormValidator.validate(
            "Laptop đấu giá", "Máy còn tốt", "1000000", startTime, endTime);

    assertTrue(result.valid());
  }

  @Test
  void blankNameFails() {
    ValidationResult result =
        SellerItemFormValidator.validate(
            " ", "Mô tả", "1000000", null, LocalDateTime.now().plusDays(1));

    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("name"));
  }

  @Test
  void nonPositivePriceFails() {
    ValidationResult result =
        SellerItemFormValidator.validate(
            "Laptop", "Mô tả", "0", null, LocalDateTime.now().plusDays(1));

    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("startPrice"));
  }

  @Test
  void startInPastWithFutureEndPassesForImmediateAuction() {
    LocalDateTime startTime = LocalDateTime.now().minusMinutes(10);
    LocalDateTime endTime = LocalDateTime.now().plusHours(1);

    ValidationResult result =
        SellerItemFormValidator.validate("Laptop", "Mô tả", "1000000", startTime, endTime, true);

    assertTrue(result.valid());
  }

  @Test
  void startAfterEndFails() {
    LocalDateTime startTime = LocalDateTime.now().plusDays(2);
    LocalDateTime endTime = startTime.minusHours(1);

    ValidationResult result =
        SellerItemFormValidator.validate("Laptop", "Mô tả", "1000000", startTime, endTime);

    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("endTime"));
  }
}
