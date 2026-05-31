package com.auction.server.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test cho User entity.
 * Tập trung: getter/setter, equals/hashCode, default values, Role enum.
 */
@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("nguyenvana");
        user.setFullName("Nguyen Van A");
        user.setEmail("a@example.com");
        user.setPasswordHash("hashed:password");
        user.setPhone("0901234567");
        user.setDateOfBirth(LocalDate.of(2000, 1, 15));
        user.setRole(User.Role.BIDDER);
        user.setIsActive(true);
        user.setBalance(BigDecimal.ZERO);
    }

    // =========================================================
    // Getter / Setter
    // =========================================================

    @Nested
    @DisplayName("Getters và Setters")
    class GetterSetterTest {

        @Test
        @DisplayName("getId/setId đúng")
        void id() {
            user.setId(42L);
            assertThat(user.getId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("getUsername/setUsername đúng")
        void username() {
            user.setUsername("newuser");
            assertThat(user.getUsername()).isEqualTo("newuser");
        }

        @Test
        @DisplayName("getFullName/setFullName đúng")
        void fullName() {
            user.setFullName("Tran Thi B");
            assertThat(user.getFullName()).isEqualTo("Tran Thi B");
        }

        @Test
        @DisplayName("getEmail/setEmail đúng")
        void email() {
            user.setEmail("b@example.com");
            assertThat(user.getEmail()).isEqualTo("b@example.com");
        }

        @Test
        @DisplayName("getPasswordHash/setPasswordHash đúng")
        void passwordHash() {
            user.setPasswordHash("salt:newhash");
            assertThat(user.getPasswordHash()).isEqualTo("salt:newhash");
        }

        @Test
        @DisplayName("getRole/setRole đúng")
        void role() {
            user.setRole(User.Role.SELLER);
            assertThat(user.getRole()).isEqualTo(User.Role.SELLER);
        }

        @Test
        @DisplayName("getBalance/setBalance đúng")
        void balance() {
            user.setBalance(new BigDecimal("500000"));
            assertThat(user.getBalance()).isEqualByComparingTo("500000");
        }

        @Test
        @DisplayName("getPhone/setPhone đúng")
        void phone() {
            user.setPhone("0912345678");
            assertThat(user.getPhone()).isEqualTo("0912345678");
        }

        @Test
        @DisplayName("getDateOfBirth/setDateOfBirth đúng")
        void dateOfBirth() {
            LocalDate dob = LocalDate.of(1995, 6, 15);
            user.setDateOfBirth(dob);
            assertThat(user.getDateOfBirth()).isEqualTo(dob);
        }

        @Test
        @DisplayName("getAvatarUrl/setAvatarUrl đúng")
        void avatarUrl() {
            user.setAvatarUrl("https://example.com/avatar.jpg");
            assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        }

        @Test
        @DisplayName("getIsActive/setIsActive đúng")
        void isActive() {
            user.setIsActive(false);
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("getCreatedAt/setCreatedAt đúng")
        void createdAt() {
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            assertThat(user.getCreatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("getUpdatedAt/setUpdatedAt đúng")
        void updatedAt() {
            LocalDateTime now = LocalDateTime.now();
            user.setUpdatedAt(now);
            assertThat(user.getUpdatedAt()).isEqualTo(now);
        }
    }

    // =========================================================
    // Default values
    // =========================================================

    @Nested
    @DisplayName("Default values")
    class DefaultValueTest {

        @Test
        @DisplayName("balance mặc định là ZERO khi tạo mới")
        void defaultBalance_isZero() {
            User fresh = new User();
            assertThat(fresh.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("role mặc định là BIDDER khi tạo mới")
        void defaultRole_isBidder() {
            User fresh = new User();
            assertThat(fresh.getRole()).isEqualTo(User.Role.BIDDER);
        }

        @Test
        @DisplayName("isActive mặc định là true khi tạo mới")
        void defaultIsActive_isTrue() {
            User fresh = new User();
            assertThat(fresh.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("items list mặc định không null, rỗng")
        void defaultItems_emptyList() {
            User fresh = new User();
            assertThat(fresh.getItems()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("bids list mặc định không null, rỗng")
        void defaultBids_emptyList() {
            User fresh = new User();
            assertThat(fresh.getBids()).isNotNull().isEmpty();
        }
    }

    // =========================================================
    // equals() và hashCode()
    // =========================================================

    @Nested
    @DisplayName("equals() và hashCode()")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("Hai user cùng id thì bằng nhau")
        void equals_sameId_areEqual() {
            User u1 = new User(); u1.setId(1L);
            User u2 = new User(); u2.setId(1L);

            assertThat(u1).isEqualTo(u2);
            assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
        }

        @Test
        @DisplayName("Hai user khác id thì không bằng nhau")
        void equals_differentId_areNotEqual() {
            User u1 = new User(); u1.setId(1L);
            User u2 = new User(); u2.setId(2L);

            assertThat(u1).isNotEqualTo(u2);
        }

        @Test
        @DisplayName("User bằng chính nó (reflexive)")
        void equals_sameInstance_isTrue() {
            assertThat(user).isEqualTo(user);
        }

        @Test
        @DisplayName("User không bằng null")
        void equals_null_isFalse() {
            assertThat(user).isNotEqualTo(null);
        }

        @Test
        @DisplayName("User không bằng object khác type")
        void equals_differentType_isFalse() {
            assertThat(user).isNotEqualTo("not a user");
        }

        @Test
        @DisplayName("Hai user null id có hashCode bằng nhau")
        void hashCode_nullId_consistent() {
            User u1 = new User();
            User u2 = new User();
            // Cả hai null id → hashCode phải giống nhau
            assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
        }

        @Test
        @DisplayName("equals là symmetric: u1.equals(u2) == u2.equals(u1)")
        void equals_isSymmetric() {
            User u1 = new User(); u1.setId(3L);
            User u2 = new User(); u2.setId(3L);

            assertThat(u1.equals(u2)).isEqualTo(u2.equals(u1));
        }
    }

    // =========================================================
    // Role enum
    // =========================================================

    @Nested
    @DisplayName("Role enum")
    class RoleEnumTest {

        @Test
        @DisplayName("3 role tồn tại: ADMIN, SELLER, BIDDER")
        void threeRolesExist() {
            User.Role[] roles = User.Role.values();
            assertThat(roles).containsExactlyInAnyOrder(
                    User.Role.ADMIN, User.Role.SELLER, User.Role.BIDDER
            );
        }

        @Test
        @DisplayName("Role.valueOf() parse đúng từ String")
        void valueOf_parsesCorrectly() {
            assertThat(User.Role.valueOf("ADMIN")).isEqualTo(User.Role.ADMIN);
            assertThat(User.Role.valueOf("SELLER")).isEqualTo(User.Role.SELLER);
            assertThat(User.Role.valueOf("BIDDER")).isEqualTo(User.Role.BIDDER);
        }
    }
}
