package com.auction.server.tools;

import com.auction.server.feature.auth.util.PasswordUtil;

/**
 * Small utility to generate password hash using server's PasswordUtil. Run from IDE or with Maven
 * exec to produce a hash you can paste into DB.
 */
public class PasswordHashGenerator {
  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      System.out.println("Usage: java PasswordHashGenerator <password>");
      System.exit(1);
    }

    String password = args[0];
    String hash = PasswordUtil.hashPassword(password);
    System.out.println(hash);
  }
}
