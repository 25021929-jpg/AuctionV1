package com.auction.validation.rules;

import com.auction.validation.ValidationRule;
import java.util.Optional;

public class PhoneNumberRule implements ValidationRule<String> {
  private static final String REGEX = "^0[35789][0-9]{8}$";

  /*
  Giải thích REGEX
  Bắt đầu bằng 0.
  Tiếp theo là một số trong tập [3, 5, 7, 8, 9].
  Kết thúc bằng đúng 8 chữ số nữa.
  Tổng là 10 chữ số
  -> Đúng định dạng số điện thoại Việt Nam
   */
  @Override
  public Optional<String> check(String phoneNumber) {
    String normalized = phoneNumber == null ? null : phoneNumber.trim().replaceAll("[\\s.-]", "");
    if (normalized == null || !normalized.matches(REGEX)) {
      return Optional.of("Sai định sạng số điện thoại");
    }
    return Optional.empty();
  }
}
