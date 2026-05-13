package com.auction.shared.validation;
import java.util.Optional;

public interface ValidationRule<T> {
    Optional<String> check(T Value);
}