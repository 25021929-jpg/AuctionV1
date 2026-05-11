package com.auction.client.feature.auth.validator;

import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.auction.client.feature.auth.dto.request.RegisterRequest;
import com.auction.validation.FieldValidator;
import com.auction.validation.ValidationResult;
import com.auction.validation.rules.EmailFormatRule;
import com.auction.validation.rules.MaxLengthRule;
import com.auction.validation.rules.MinLengthRule;
import com.auction.validation.rules.NotBlankRule;

/**
 * AuthValidator chịu trách nhiệm validate dữ liệu cho các form Auth.
 *
 * Nguyên lý áp dụng:
 *  - SRP: chỉ lo validate, không chứa business logic.
 *  - DRY: KHÔNG tự viết lại logic check – tái dụng Rules từ Auction-shared.
 *  - DIP: phụ thuộc vào abstraction (FieldValidator, ValidationRule) từ shared,
 *         không phụ thuộc implementation cụ thể.
 *
 * AuthValidator là lớp "mỏng" – chỉ CẤU HÌNH các rule cho từng field,
 * còn ENGINE validate nằm hoàn toàn ở Auction-shared.
 */
public class AuthValidator {

    // Inject FieldValidator từ shared (engine thực sự chạy rule)
    private final FieldValidator fieldValidator;

    public AuthValidator(FieldValidator fieldValidator) {
        this.fieldValidator = fieldValidator;
    }

    // =========================================================
    // VALIDATE LOGIN
    // =========================================================

    /**
     * Validate form đăng nhập.
     *
     * Rules:
     *  - email  : không rỗng, đúng định dạng email
     *  - password: không rỗng, tối thiểu 6 ký tự
     *
     * @param dto dữ liệu form đăng nhập
     * @return ValidationResult chứa danh sách lỗi (nếu có)
     */
    public ValidationResult validateLogin(LoginRequest dto) {
        ValidationResult result = new ValidationResult();

        // Validate field "email"
        result.merge(
            fieldValidator.validate(
                "email",
                dto.getEmail(),
                new NotBlankRule(),
                new EmailFormatRule()
            )
        );

        // Validate field "password"
        result.merge(
            fieldValidator.validate(
                "password",
                dto.getPassword(),
                new NotBlankRule(),
                new MinLengthRule(6)
            )
        );

        return result;
    }

    // =========================================================
    // VALIDATE REGISTER
    // =========================================================

    /**
     * Validate form đăng ký.
     *
     * Rules:
     *  - username: không rỗng, 3–50 ký tự
     *  - email   : không rỗng, đúng định dạng
     *  - password: không rỗng, 6–100 ký tự
     *
     * @param dto dữ liệu form đăng ký
     * @return ValidationResult chứa danh sách lỗi (nếu có)
     */
    public ValidationResult validateRegister(RegisterDto dto) {
        ValidationResult result = new ValidationResult();

        // Validate field "username"
        result.merge(
            fieldValidator.validate(
                "username",
                dto.getUsername(),
                new NotBlankRule(),
                new MinLengthRule(3),
                new MaxLengthRule(50)
            )
        );

        // Validate field "email"
        result.merge(
            fieldValidator.validate(
                "email",
                dto.getEmail(),
                new NotBlankRule(),
                new EmailFormatRule()
            )
        );

        // Validate field "password"
        result.merge(
            fieldValidator.validate(
                "password",
                dto.getPassword(),
                new NotBlankRule(),
                new MinLengthRule(6),
                new MaxLengthRule(100)
            )
        );

        return result;
    }
}
