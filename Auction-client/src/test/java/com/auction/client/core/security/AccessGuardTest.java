package com.auction.client.core.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.client.core.error.ForbiddenException;
import com.auction.client.core.error.UnauthorizedException;
import com.auction.client.core.session.UserSession;
import com.auction.shared.domain.UserRole;
import com.auction.shared.dto.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AccessGuardTest {

  @AfterEach
  void tearDown() {
    UserSession.getInstance().clear();
  }

  @Test
  void requireLoginShouldRejectGuest() {
    UserSession.getInstance().clear();
    assertThrows(UnauthorizedException.class, AccessGuard::requireLogin);
  }

  @Test
  void requireLoginShouldAllowLoggedInUser() {
    loginAs("BIDDER");
    assertDoesNotThrow(AccessGuard::requireLogin);
  }

  @Test
  void requireRoleShouldRejectWrongRole() {
    loginAs("BIDDER");
    assertThrows(ForbiddenException.class, () -> AccessGuard.requireRole(UserRole.ADMIN));
  }

  @Test
  void requireAnyRoleShouldAllowOneMatchingRole() {
    loginAs("SELLER");
    assertDoesNotThrow(() -> AccessGuard.requireAnyRole(UserRole.SELLER, UserRole.ADMIN));
  }

  private void loginAs(String role) {
    UserSession.getInstance()
        .setCurrentUser(
            new UserInfo(1L, "Test User", "tester", "tester@example.com", "", "", role));
  }
}
