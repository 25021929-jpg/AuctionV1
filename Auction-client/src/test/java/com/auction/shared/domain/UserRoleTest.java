package com.auction.shared.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserRoleTest {

  @Test
  void fromString_nullOrBlank_returnsNull() {
    assertNull(UserRole.fromString(null));
    assertNull(UserRole.fromString(""));
    assertNull(UserRole.fromString("   "));
  }

  @Test
  void fromString_caseInsensitive_parses() {
    assertEquals(UserRole.BIDDER, UserRole.fromString("bidder"));
    assertEquals(UserRole.SELLER, UserRole.fromString("SELLER"));
    assertEquals(UserRole.ADMIN, UserRole.fromString("Admin"));
  }

  @Test
  void fromString_unknown_returnsNull() {
    assertNull(UserRole.fromString("SUPER_ADMIN"));
  }
}
