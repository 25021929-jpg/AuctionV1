package com.auction.server.feature.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Class tiện ích dùng để mã hóa và kiểm tra mật khẩu
public class PasswordUtil {

    // Mã hóa mật khẩu bằng SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Chuyển password thành mảng byte rồi mã hóa
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Chuyển byte sang chuỗi hex
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);

                // Nếu hex chỉ có 1 ký tự thì thêm số 0 phía trước
                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot hash password");
        }
    }

    // So sánh password người dùng nhập với password đã mã hóa trong database
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        String rawPasswordHash = hashPassword(rawPassword);

        return rawPasswordHash.equals(hashedPassword);
    }
}