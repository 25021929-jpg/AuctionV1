package com.auction.shared.dto;

public class AuthResponse {

  private com.auction.shared.dto.UserInfo user;

  // Bắt buộc phải có — Gson dùng cái này
  public AuthResponse() {}

  // Giữ lại constructor có tham số — server dùng khi tạo response
  public AuthResponse(UserInfo user) {
    this.user = user;
  }

  public static AuthResponse fromUserInfo(UserInfo userInfo) {
    return new AuthResponse(userInfo);
  }

  public UserInfo getUser() {
    return user;
  }
}
