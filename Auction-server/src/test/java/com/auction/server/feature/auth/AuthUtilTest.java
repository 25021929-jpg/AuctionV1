package com.auction.server.feature.auth;

import com.auction.server.feature.auth.AuthException;
import com.auction.server.feature.auth.util.PasswordUtil;
import com.auction.server.feature.auth.util.ResetTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test cho PasswordUtil và ResetTokenUtil.
 *
 * PasswordUtil dùng PBKDF2WithHmacSHA256 — không cần mock, chạy thuần JDK.
 * ResetTokenUtil dùng SecureRandom + Base64 — kiểm tra tính ngẫu nhiên và format.
 */
@DisplayName("Auth Utility Tests")
class AuthUtilTest {

    // =========================================================
    // PasswordUtil
    // =========================================================

    @Nested
    @DisplayName("PasswordUtil")
    class PasswordUtilTest {

        @Test
        @DisplayName("hashPassword() trả về chuỗi không null, không rỗng")
        void hashPassword_returnsNonBlankString() {
            String hash = PasswordUtil.hashPassword("mypassword");
            assertThat(hash).isNotBlank();
        }



        @Test
        @DisplayName("hashPassword() trả về định dạng salt:hash (có dấu :)")
        void hashPassword_returnsCorrectFormat() {
            String hash = PasswordUtil.hashPassword("mypassword");
            // Format: base64salt:base64hash
            assertThat(hash).contains(":");
            String[] parts = hash.split(":");
            assertThat(parts).hasSize(2);
            assertThat(parts[0]).isNotBlank(); // salt
            assertThat(parts[1]).isNotBlank(); // hash
        }

        @Test
        @DisplayName("hashPassword() với cùng password tạo ra 2 hash KHÁC nhau (salt ngẫu nhiên)")
        void hashPassword_samePassword_producesDifferentHashes() {
            String hash1 = PasswordUtil.hashPassword("samepassword");
            String hash2 = PasswordUtil.hashPassword("samepassword");
            // Hai hash phải khác nhau do salt ngẫu nhiên mỗi lần
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("hashPassword() không trả về plaintext password")
        void hashPassword_doesNotContainPlaintext() {
            String password = "secret123";
            String hash = PasswordUtil.hashPassword(password);
            assertThat(hash).doesNotContain(password);
        }

        @Test
        @DisplayName("verifyPassword() trả về true khi password đúng")
        void verifyPassword_correctPassword_returnsTrue() {
            String password = "correctPass!";
            String hash = PasswordUtil.hashPassword(password);
            assertThat(PasswordUtil.verifyPassword(password, hash)).isTrue();
        }

        @Test
        @DisplayName("verifyPassword() trả về false khi password sai")
        void verifyPassword_wrongPassword_returnsFalse() {
            String hash = PasswordUtil.hashPassword("correctPass!");
            assertThat(PasswordUtil.verifyPassword("wrongPass!", hash)).isFalse();
        }

        @Test
        @DisplayName("verifyPassword() trả về false khi hash bị hỏng (không có dấu :)")
        void verifyPassword_malformedHash_returnsFalse() {
            assertThat(PasswordUtil.verifyPassword("anypassword", "notavalidhash")).isFalse();
        }

        @Test
        @DisplayName("verifyPassword() trả về false khi storedHash rỗng")
        void verifyPassword_emptyHash_returnsFalse() {
            assertThat(PasswordUtil.verifyPassword("anypassword", "")).isFalse();
        }

        @Test
        @DisplayName("verifyPassword() trả về false khi password rỗng")
        void verifyPassword_emptyPassword_returnsFalse() {
            String hash = PasswordUtil.hashPassword("realpassword");
            assertThat(PasswordUtil.verifyPassword("", hash)).isFalse();
        }

        @Test
        @DisplayName("verifyPassword() phân biệt hoa/thường")
        void verifyPassword_caseSensitive() {
            String hash = PasswordUtil.hashPassword("Password123");
            assertThat(PasswordUtil.verifyPassword("password123", hash)).isFalse();
            assertThat(PasswordUtil.verifyPassword("PASSWORD123", hash)).isFalse();
        }

        @Test
        @DisplayName("verifyPassword() phân biệt khoảng trắng")
        void verifyPassword_spaceSensitive() {
            String hash = PasswordUtil.hashPassword("pass word");
            assertThat(PasswordUtil.verifyPassword("password", hash)).isFalse();
        }

        @Test
        @DisplayName("hashPassword() hoạt động với password đặc biệt (ký tự unicode, dài)")
        void hashPassword_specialCharsAndLongPassword_doesNotThrow() {
            assertThat(PasswordUtil.hashPassword("P@$$w0rd!#%^&*")).isNotBlank();
            assertThat(PasswordUtil.hashPassword("Mật khẩu tiếng Việt 123!")).isNotBlank();
            assertThat(PasswordUtil.hashPassword("a".repeat(200))).isNotBlank();
        }
    }

    // =========================================================
    // ResetTokenUtil
    // =========================================================

    @Nested
    @DisplayName("ResetTokenUtil")
    class ResetTokenUtilTest {

        @Test
        @DisplayName("generateToken() trả về chuỗi không null, không rỗng")
        void generateToken_returnsNonBlankString() {
            assertThat(ResetTokenUtil.generateToken()).isNotBlank();
        }

        @Test
        @DisplayName("generateToken() trả về token URL-safe (không có +, /, =)")
        void generateToken_returnsUrlSafeString() {
            String token = ResetTokenUtil.generateToken();
            // Base64 URL-safe without padding: chỉ có A-Z, a-z, 0-9, -, _
            assertThat(token).matches("[A-Za-z0-9_-]+");
        }

        @Test
        @DisplayName("generateToken() trả về độ dài đủ dài (32 bytes → ~43 ký tự base64url)")
        void generateToken_hasAdequateLength() {
            String token = ResetTokenUtil.generateToken();
            // 32 bytes → 43 chars base64url (without padding)
            assertThat(token.length()).isGreaterThanOrEqualTo(40);
        }

        @RepeatedTest(10)
        @DisplayName("generateToken() trả về token khác nhau mỗi lần gọi")
        void generateToken_isUnique() {
            Set<String> tokens = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                tokens.add(ResetTokenUtil.generateToken());
            }
            // 10 lần gọi phải cho 10 token khác nhau
            assertThat(tokens).hasSize(10);
        }
    }
}