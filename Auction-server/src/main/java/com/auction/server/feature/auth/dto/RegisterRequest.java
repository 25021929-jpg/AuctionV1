package com.auction.server.feature.auth.dto;

public class RegisterRequest {

  private String fullName;
  private String username;
  private String email;
  private String phone;
  private String dateOfBirth; // dạng yyyy-MM-dd
  private String password;
  private String confirmPassword;
  private String role; // BIDDER hoặc SELLER

  public RegisterRequest() {}

  public String getFullName() {
    return fullName;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public String getPassword() {
    return password;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public String getRole() {
    return role;
  }
}
