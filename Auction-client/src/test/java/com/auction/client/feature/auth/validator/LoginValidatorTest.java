package com.auction.client.feature.auth.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.shared.dto.auth.request.LoginRequest;
import com.auction.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginValidatorTest {

  private LoginValidator validator;

  @BeforeEach
  void setUp() {
    validator = new LoginValidator();
  }

  // ─── identity ───────────────────────────────────────

  @Test
  @DisplayName("Tên đăng nhập hợp lệ -> pass")
  void validate_validUsername_returnsOk() {
    ValidationResult result = validator.validate(new LoginRequest("john", "password123"));
    assertTrue(result.valid());
  }

  @Test
  @DisplayName("Tên email hợp lệ -> pass")
  void validate_validEmail_returnsOk() {
    ValidationResult result = validator.validate(new LoginRequest("john@gmail.com", "password123"));
    assertTrue(result.valid());
  }

  @Test
  @DisplayName("Để trống Indentity -> error")
  void validate_emptyIdentity_returnsError() {
    ValidationResult result = validator.validate(new LoginRequest("", "password123"));
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("identity"));
    assertEquals("Thành phần không được để trống", result.errorFor("identity"));
  }

  @Test
  @DisplayName("Sai định dạng email -> error")
  void validate_invalidEmailFormat_returnsError() {
    ValidationResult result = validator.validate(new LoginRequest("john@", "password123"));
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("identity"));
    assertEquals("Sai định dạng email", result.errorFor("identity"));
  }

  // ─── password ───────────────────────────────────────

  @Test
  @DisplayName("Để trống password -> error")
  void validate_emptyPassword_returnsError() {
    ValidationResult result = validator.validate(new LoginRequest("john", ""));
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("password"));
    assertEquals("Thành phần không được để trống", result.errorFor("password"));
  }

  // ─── cả 2 trống ─────────────────────────────────────

  @Test
  @DisplayName("Cả 2 đều bỏ trống-> 2 errors")
  void validate_bothEmpty_returnsTwoErrors() {
    ValidationResult result = validator.validate(new LoginRequest("", ""));
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("identity"));
    assertTrue(result.hasErrorFor("password"));
  }
}
