package com.auction.validation.rules;

import com.auction.validation.ValidationRule;

import java.util.Optional;

public class MaxLengthRule implements ValidationRule<String> {
    private int max;
    public MaxLengthRule(int max){
        this.max = max;
    }
    public Optional<String> check(String value){
        return (value != null && value.length()<= max)
                ? Optional.empty()
                : Optional.of("Must be at most " + max + " characters");
    }
}
