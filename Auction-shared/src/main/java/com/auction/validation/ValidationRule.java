package com.auction.validation;

import java.util.Optional;

public interface ValidationRule<T> {
  Optional<String> check(T Value);
}
