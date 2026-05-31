package com.auction.client.core.session;

import com.auction.shared.domain.UserRole;
import com.auction.shared.dto.AuthResponse;
import com.auction.shared.dto.UserInfo;
import java.math.BigDecimal;

/**
 * Lưu trạng thái đăng nhập hiện tại ở phía client.
 *
 * <p>Client chỉ lưu DTO an toàn nhận từ server, không lưu entity/domain có thể chứa dữ liệu nhạy
 * cảm.
 */
public final class UserSession {

  private static final UserSession INSTANCE = new UserSession();

  private UserInfo currentUser;
  private String token;

  private UserSession() {}

  public static UserSession getInstance() {
    return INSTANCE;
  }

  public UserInfo getCurrentUser() {
    return currentUser;
  }

  public void start(AuthResponse authResponse) {
    if (authResponse == null) {
      clear();
      return;
    }
    setCurrentUser(authResponse.getUser());
  }

  public void setCurrentUser(UserInfo currentUser) {
    this.currentUser = currentUser;
  }

  public Long getUserId() {
    return currentUser == null ? null : currentUser.getId();
  }

  public String getUsername() {
    return currentUser == null ? null : currentUser.getUsername();
  }

  public BigDecimal getBalance() {
    return currentUser == null || currentUser.getBalance() == null
        ? BigDecimal.ZERO
        : currentUser.getBalance();
  }

  public void updateBalance(BigDecimal newBalance) {
    if (currentUser != null) {
      currentUser.setBalance(newBalance);
    }
  }

  public UserRole getRole() {
    if (currentUser == null) {
      return null;
    }
    return UserRole.fromString(currentUser.getRole());
  }

  public boolean hasRole(UserRole role) {
    return role != null && role == getRole();
  }

  public boolean isLoggedIn() {
    return currentUser != null;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String displayName() {
    if (currentUser == null) {
      return "Khách";
    }
    if (currentUser.getFullName() != null && !currentUser.getFullName().isBlank()) {
      return currentUser.getFullName();
    }
    if (currentUser.getUsername() != null && !currentUser.getUsername().isBlank()) {
      return currentUser.getUsername();
    }
    return "Người dùng #" + currentUser.getId();
  }

  public void clear() {
    this.currentUser = null;
    this.token = null;
  }
}
