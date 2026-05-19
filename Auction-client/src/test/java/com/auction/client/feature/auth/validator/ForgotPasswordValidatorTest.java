package com.auction.client.feature.auth.validator;

import com.auction.client.feature.auth.dto.request.ForgotPasswordRequest;
import com.auction.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ForgotPasswordValidatorTest {
    private ForgotPasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ForgotPasswordValidator();
    }

    @Test
    @DisplayName("Email hợp lệ -> pass")
    void validate_validEmail_returnsOk() {
        ValidationResult result = validator.validate(
                new ForgotPasswordRequest("john@gmail.com")
        );
        assertTrue(result.valid());
    }

    @Test
    @DisplayName("Email trống -> error")
    void validate_emptyEmail_returnsError() {
        ValidationResult result = validator.validate(
                new ForgotPasswordRequest("")
        );
        assertFalse(result.valid());
        assertTrue(result.hasErrorFor("email"));
        assertEquals("Thành phần không được để trống",
                result.errorFor("email"));
    }

    @Test
    @DisplayName("Sai định dạng email -> error")
    void validate_invalidEmail_returnsError() {
        ValidationResult result = validator.validate(
                new ForgotPasswordRequest("john@")
        );
        assertFalse(result.valid());
        assertTrue(result.hasErrorFor("email"));
        assertEquals("Email không hợp lệ",
                result.errorFor("email"));
    }

    @Test
    @DisplayName("Null email -> error")
    void validate_nullEmail_returnsError() {
        ValidationResult result = validator.validate(
                new ForgotPasswordRequest(null)
        );
        assertFalse(result.valid());
        assertTrue(result.hasErrorFor("email"));
    }
}
