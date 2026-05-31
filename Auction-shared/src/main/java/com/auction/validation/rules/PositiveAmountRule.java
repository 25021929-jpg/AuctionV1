package com.auction.validation.rules;

import com.auction.validation.ValidationRule;
import java.util.Optional;

/**
 * Kiểm tra giá tiền nhập từ UI phải là số dương.
 *
 * <p>Rule này chỉ validate định dạng và điều kiện > 0 ở phía client/shared. Quy tắc nghiệp vụ "giá
 * đặt phải lớn hơn giá hiện tại" vẫn phải được kiểm tra ở tầng đấu giá của server để tránh race
 * condition.
 */
public class PositiveAmountRule implements ValidationRule<String> {

  @Override
  public Optional<String> check(String value) {
    if (value == null || value.trim().isEmpty()) {
      return Optional.of("Giá không được để trống");
    }

    final double amount;
    try {
      amount = Double.parseDouble(value.trim());
    } catch (NumberFormatException ex) {
      return Optional.of("Giá phải là số hợp lệ");
    }

    if (!Double.isFinite(amount)) {
      return Optional.of("Giá phải là số hữu hạn");
    }
    if (amount <= 0) {
      return Optional.of("Giá phải lớn hơn 0");
    }
    return Optional.empty();
  }
}
