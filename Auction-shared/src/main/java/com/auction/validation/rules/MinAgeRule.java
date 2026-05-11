package com.auction.validation.rules;

import com.auction.validation.ValidationRule;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

public class MinAgeRule implements ValidationRule<LocalDate> {
    private final int minAge;
    public MinAgeRule(int minAge){
        this.minAge = minAge;
    }
    public Optional<String> check(LocalDate date){
        if (date == null){
            return Optional.of("Date of birth is required");
        }
        int age = Period.between(date, LocalDate.now()).getYears();
        if (age < minAge){
            return Optional.of("Must be at least " + minAge + " years old");
        }
        return Optional.empty();
    }
}
