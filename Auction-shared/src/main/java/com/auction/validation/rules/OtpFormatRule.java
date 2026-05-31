package com.auction.validation.rules;

import com.auction.validation.ValidationRule;
import java.util.Optional;

public class OtpFormatRule implements ValidationRule<String> {

  @Override
  public Optional<String> check(String value) {
    if (value == null || !value.matches("\\d{6}")) {
      return Optional.of("Mã OTP phải gồm đúng 6 chữ số");
    }
    return Optional.empty();
  }
}
