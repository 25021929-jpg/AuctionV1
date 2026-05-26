package com.auction.shared.dto.auth.request;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Request đăng ký dùng chung giữa client và server.
 *
 * <p>Trên wire JSON ưu tiên field server-friendly: phone, dateOfBirth (yyyy-MM-dd).
 * Client vẫn có các method dạng record cũ như phoneNumber(), birthDate() để các
 * validator/controller không phải giữ DTO riêng ở module client.</p>
 */
public class RegisterRequest {
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String password;
    private String confirmPassword;

    /** Không serialize field này; chỉ để client validate tuổi bằng LocalDate. */
    private transient LocalDate birthDate;

    public RegisterRequest() {
    }

    /** Constructor tiện cho JavaFX DatePicker ở client. */
    public RegisterRequest(String fullName, String username, String email, String phoneNumber,
                           String password, String confirmPassword, LocalDate birthDate) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phoneNumber;
        this.birthDate = birthDate;
        this.dateOfBirth = birthDate == null ? null : birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        syncBirthDateFromString();
    }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    // Record-like accessors để thay thế DTO cũ của client.
    public String fullName() { return fullName; }
    public String username() { return username; }
    public String email() { return email; }
    public String phoneNumber() { return phone; }
    public String phone() { return phone; }
    public String dateOfBirth() { return dateOfBirth; }
    public String password() { return password; }
    public String confirmPassword() { return confirmPassword; }
    public LocalDate birthDate() { return birthDate; }

    private void syncBirthDateFromString() {
        if (dateOfBirth == null || dateOfBirth.isBlank()) {
            birthDate = null;
            return;
        }
        try {
            birthDate = LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ignored) {
            birthDate = null;
        }
    }
}
