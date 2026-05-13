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
            return Optional.of("Ngày sinh không được để trống");
        }
        int age = Period.between(date, LocalDate.now()).getYears();
        if (age < minAge){
            return Optional.of("Người dùng phải " + minAge + " tuổi trở lên");
        }
        return Optional.empty();
    }
}
