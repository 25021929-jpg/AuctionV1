package com.auction.client.feature.auth.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.shared.dto.auth.request.RegisterRequest;
import com.auction.validation.ValidationResult;
import java.time.LocalDate;
import org.junit.jupiter.api.*;

class RegisterValidatorTest {

  private RegisterValidator validator;

  // LocalDate hợp lệ — đủ 18 tuổi
  private static final LocalDate VALID_DATE = LocalDate.now().minusYears(20);

  // LocalDate chưa đủ 18 tuổi
  private static final LocalDate UNDERAGE_DATE = LocalDate.now().minusYears(16);

  @BeforeEach
  void setUp() {
    validator = new RegisterValidator();
  }

  // Helper tạo request hợp lệ — tái dùng trong nhiều test
  private RegisterRequest validRequest() {
    return new RegisterRequest(
        "Nguyen Van A",
        "john123",
        "john@gmail.com",
        "0912345678",
        "password123",
        "password123",
        VALID_DATE);
  }

  // ── Full valid ───────────────────────────────────────

  @Test
  @DisplayName("All valid -> pass")
  void validate_allValid_returnsOk() {
    ValidationResult result = validator.validate(validRequest());
    assertTrue(result.valid());
  }

  // ── fullName ─────────────────────────────────────────

  @Test
  @DisplayName("Để trống tên -> error")
  void validate_emptyFullName_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "",
            "john123",
            "john@gmail.com",
            "0912345678",
            "password123",
            "password123",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("fullName"));
    assertEquals("Thành phần không được để trống", result.errorFor("fullName"));
  }

  @Test
  @DisplayName("Tên có ký tự chũ số -> error")
  void validate_fullNameWithNumbers_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "John123",
            "john123",
            "john@gmail.com",
            "0912345678",
            "password123",
            "password123",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("fullName"));
  }

  // ── username ─────────────────────────────────────────

  @Test
  @DisplayName("Để trống tên đăng nhập-> error")
  void validate_emptyUsername_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "",
            "john@gmail.com",
            "0912345678",
            "password123",
            "password123",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("username"));
    assertEquals("Thành phần không được để trống", result.errorFor("username"));
  }

  @Test
  @DisplayName("Tên đăng nhập quá ngắng -> error")
  void validate_usernameTooShort_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "ab",
            "john@gmail.com",
            "0912345678",
            "password123",
            "password123",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("username"));
    assertEquals("Phải chứa ít nhất 3 ký tự", result.errorFor("username"));
  }

  @Test
  @DisplayName("Tên đăng nhập quá dài -> error")
  void validate_usernameTooLong_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "a".repeat(21),
            "john@gmail.com",
            "0912345678",
            "password123",
            "password123",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("username"));
  }

  // ── email ────────────────────────────────────────────

  @Test
  @DisplayName("Invalid email -> error")
  void validate_invalidEmail_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "john123",
            "john@",
            "0912345678",
            "password123",
            "password123",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("email"));
    assertEquals("Email không hợp lệ", result.errorFor("email"));
  }

  // ── phoneNumber ──────────────────────────────────────

  @Test
  @DisplayName("Phone with spaces -> pass")
  void validate_phoneWithSpaces_returnsOk() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "john123",
            "john@gmail.com",
            "0912 345 678",
            "password123",
            "password123",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertTrue(result.valid());
  }

  @Test
  @DisplayName("Invalid phone -> error")
  void validate_invalidPhone_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "john123",
            "john@gmail.com",
            "123456789",
            "password123",
            "password123",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("phoneNumber"));
  }

  // ── password ─────────────────────────────────────────

  @Test
  @DisplayName("Password too short -> error")
  void validate_passwordTooShort_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A", "john123", "john@gmail.com", "0912345678", "abc", "abc", VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("password"));
    assertEquals("Phải chứa ít nhất 8 ký tự", result.errorFor("password"));
  }

  // ── confirmPassword ──────────────────────────────────

  @Test
  @DisplayName("Passwords not match -> error")
  void validate_passwordNotMatch_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "john123",
            "john@gmail.com",
            "0912345678",
            "password123",
            "different",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("confirmPassword"));
    assertEquals("Mật khẩu không khớp", result.errorFor("confirmPassword"));
  }

  @Test
  @DisplayName("Password has error -> skip confirmPassword check")
  void validate_passwordError_skipsConfirmCheck() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "john123",
            "john@gmail.com",
            "0912345678",
            "abc",
            "different",
            VALID_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("password"));
    // confirmPassword không check khi password đang lỗi
    assertFalse(result.hasErrorFor("confirmPassword"));
  }

  // ── birthDate ────────────────────────────────────────

  @Test
  @DisplayName("Underage -> error")
  void validate_underage_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "john123",
            "john@gmail.com",
            "0912345678",
            "password123",
            "password123",
            UNDERAGE_DATE);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("birthDate"));
    assertEquals("Người dùng phải từ 18 tuổi trở lên", result.errorFor("birthDate"));
  }

  @Test
  @DisplayName("Null birthDate -> error")
  void validate_nullBirthDate_returnsError() {
    RegisterRequest req =
        new RegisterRequest(
            "Nguyen Van A",
            "john123",
            "john@gmail.com",
            "0912345678",
            "password123",
            "password123",
            null);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("birthDate"));
  }

  // ── Multiple errors ──────────────────────────────────

  @Test
  @DisplayName("Multiple fields invalid -> multiple errors")
  void validate_multipleInvalid_returnsMultipleErrors() {
    RegisterRequest req = new RegisterRequest("", "", "", "", "", "", null);
    ValidationResult result = validator.validate(req);
    assertFalse(result.valid());
    assertTrue(result.hasErrorFor("fullName"));
    assertTrue(result.hasErrorFor("username"));
    assertTrue(result.hasErrorFor("email"));
    assertTrue(result.hasErrorFor("phoneNumber"));
    assertTrue(result.hasErrorFor("password"));
    assertTrue(result.hasErrorFor("birthDate"));
  }
}
