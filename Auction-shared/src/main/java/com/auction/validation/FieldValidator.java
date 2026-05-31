package com.auction.validation;

import java.util.Optional;

// Sử dụng class FieldValidator để biết lỗi thuộc field nào
// Kiểm tra Field theo các lỗi
// Gắn rule chain với tên Field
public class FieldValidator<T> {
  private final String fieldName;
  private final RuleChain<T> chain;
  private final T value;

  // Constructor
  @SafeVarargs
  public FieldValidator(String fieldName, T value, ValidationRule<T>... rules) {
    this.fieldName = fieldName;
    this.value = value;
    this.chain = new RuleChain<>(rules);
  }

  // Associate Field với lỗi của nó
  public Optional<FieldError> validate() {
    return chain.run(value).map(msg -> new FieldError(fieldName, msg));
  }
}
