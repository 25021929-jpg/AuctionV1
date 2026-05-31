package com.auction.server.feature.auth.util;

import java.security.SecureRandom;
import java.util.Base64;

public class ResetTokenUtil {

  private static final SecureRandom random = new SecureRandom();

  // Tạo token random an toàn
  public static String generateToken() {

    byte[] bytes = new byte[32];

    random.nextBytes(bytes);

    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
