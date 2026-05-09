package com.auction.validation.rules;

import com.auction.validation.ValidationRule;

import java.util.Optional;

public class MinLengthRule implements ValidationRule<String> {

    private final int min;

    public MinLengthRule(int min){

        this.min = min ;
    }

    public Optional<String> check(String value){
        return (value != null && value.length() >= min)
                ? Optional.empty()
                : Optional.of("Phải có ít nhất " + min + " Ký tự");
    }
}
