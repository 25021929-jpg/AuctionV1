package com.auction.server.feature.auth.util;

import com.auction.server.feature.auth.AuthException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    // Hash password khi đăng ký hoặc reset password
    public static String hashPassword(String password) {
        try {
            byte[] salt = generateSalt();
            byte[] hash = pbkdf2(password, salt);

            // Lưu dạng: salt:hash
            return Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new AuthException("Cannot hash password");
        }
    }

    // Kiểm tra password người dùng nhập có khớp passwordHash trong DB không
    public static boolean verifyPassword(String rawPassword, String storedPasswordHash) {
        try {
            String[] parts = storedPasswordHash.split(":");

            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);

            byte[] inputHash = pbkdf2(rawPassword, salt);

            return slowEquals(storedHash, inputHash);

        } catch (Exception e) {
            return false;
        }
    }

    // Tạo salt ngẫu nhiên để mỗi password có hash khác nhau
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();

        byte[] salt = new byte[SALT_LENGTH];

        random.nextBytes(salt);

        return salt;
    }

    // Thuật toán hash password
    private static byte[] pbkdf2(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        return factory.generateSecret(spec).getEncoded();
    }

    // So sánh hash an toàn hơn so với equals thông thường
    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;

        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }

        return result == 0;
    }
}