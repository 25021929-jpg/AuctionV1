package com.auction.shared.dto.auth.request;

/** Request đặt lại mật khẩu. */
public class ResetPasswordRequest {
  private String token;
  private String newPassword;
  private String confirmPassword;

  public ResetPasswordRequest() {}

  /** Constructor dùng ở client khi token chưa được server hóa. */
  public ResetPasswordRequest(String newPassword, String confirmPassword) {
    this(null, newPassword, confirmPassword);
  }

  public ResetPasswordRequest(String token, String newPassword, String confirmPassword) {
    this.token = token;
    this.newPassword = newPassword;
    this.confirmPassword = confirmPassword;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  // Record-like accessors để thay DTO record cũ ở client.
  public String token() {
    return token;
  }

  public String password() {
    return newPassword;
  }

  public String newPassword() {
    return newPassword;
  }

  public String confirmPassword() {
    return confirmPassword;
  }
}
