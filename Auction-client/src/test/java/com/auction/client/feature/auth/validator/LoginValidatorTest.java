package com.auction.client.feature.auth.validator;

import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.auction.client.feature.auth.factory.AuthValidatorFactory;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginValidator")
class LoginValidatorTest {

    private Validator<LoginRequest> validator;

    @BeforeEach
    void setUp() {
        // Dùng Factory đúng như production code — test cả Factory luôn
        validator = AuthValidatorFactory.createLoginValidator();
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Hợp lệ")
    class Valid {

        @Test
        @DisplayName("Đăng nhập bằng username + password")
        void usernameAndPassword() {
            var req = new LoginRequest("nguyen_van_a", "password123");
            assertTrue(validator.validate(req).valid());
        }

        @Test
        @DisplayName("Đăng nhập bằng email + password")
        void emailAndPassword() {
            var req = new LoginRequest("user@gmail.com", "abcdef12");
            assertTrue(validator.validate(req).valid());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Identity field
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Field: identity")
    class IdentityField {

        @Test
        @DisplayName("Bỏ trống → lỗi identity")
        void blank_fails() {
            var result = validator.validate(new LoginRequest("", "password123"));
            assertFalse(result.valid());
            assertTrue(hasError(result, "identity"));
        }

        @Test
        @DisplayName("Chỉ khoảng trắng → lỗi identity")
        void whitespaceOnly_fails() {
            var result = validator.validate(new LoginRequest("   ", "password123"));
            assertFalse(result.valid());
            assertTrue(hasError(result, "identity"));
        }

        @Test
        @DisplayName("null → lỗi identity")
        void null_fails() {
            var result = validator.validate(new LoginRequest(null, "password123"));
            assertFalse(result.valid());
            assertTrue(hasError(result, "identity"));
        }

        @Test
        @DisplayName("Email sai định dạng → lỗi identity")
        void invalidEmailFormat_fails() {
            var result = validator.validate(new LoginRequest("not-an-email@", "password123"));
            assertFalse(result.valid());
            assertTrue(hasError(result, "identity"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Password field
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Field: password")
    class PasswordField {

        @Test
        @DisplayName("Bỏ trống → lỗi password")
        void blank_fails() {
            var result = validator.validate(new LoginRequest("user@gmail.com", ""));
            assertFalse(result.valid());
            assertTrue(hasError(result, "password"));
        }

        @Test
        @DisplayName("null → lỗi password")
        void null_fails() {
            var result = validator.validate(new LoginRequest("user@gmail.com", null));
            assertFalse(result.valid());
            assertTrue(hasError(result, "password"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Cả hai field sai cùng lúc
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Cả hai field cùng sai")
    class BothInvalid {

        @Test
        @DisplayName("Cả identity và password đều trống → 2 lỗi")
        void bothBlank_twoErrors() {
            var result = validator.validate(new LoginRequest("", ""));

            assertFalse(result.valid());
            // Kiểm tra đích danh xem cả 2 field này đều đã bị bắt lỗi thành công
            assertTrue(hasError(result, "identity"));
            assertTrue(hasError(result, "password"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────
    private boolean hasError(ValidationResult result, String field) {
        return result.hasErrorFor(field);
    }
}