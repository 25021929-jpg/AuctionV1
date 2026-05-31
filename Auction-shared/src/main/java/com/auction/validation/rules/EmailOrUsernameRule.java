package com.auction.validation.rules;

import com.auction.validation.ValidationRule;
import java.util.Optional;

public class EmailOrUsernameRule implements ValidationRule<String> {

  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  @Override
  public Optional<String> check(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty(); // NotBlankRule xử lý lỗi rỗng.
    }

    String normalized = value.trim();
    if (normalized.contains("@") && !normalized.matches(EMAIL_REGEX)) {
      return Optional.of("Sai định dạng email");
    }

    return Optional.empty();
  }
}
