package com.auction.validation.rules;

import com.auction.validation.ValidationRule;
import java.util.Optional;

public class FullNameRule implements ValidationRule<String> {

  private static final String REGEX = "^[\\p{L}\\s]+$";

  @Override
  public Optional<String> check(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty(); // NotBlankRule chịu trách nhiệm báo lỗi rỗng.
    }
    if (!value.trim().matches(REGEX)) {
      return Optional.of("Tên chỉ được chứa chữ cái và khoảng trắng");
    }
    return Optional.empty();
  }
}
