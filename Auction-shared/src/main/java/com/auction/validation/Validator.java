package com.auction.validation;
/*
    Interface cho business Validator
    validate() return ValidationResult vì đó là kiểu duy nhất trả lời đủ 3 câu hỏi mà Controller cần:
    +)form có hợp lệ không, field nào lỗi, lỗi gì. Các kiểu khác đều trả lời thiếu ít nhất 1 câu.
    +)Tuần theo Strategy design pattern
 */
public interface Validator<T> {
    ValidationResult validate(T request);

    ValidationResult validate(ResetPasswordRequest request);
}
