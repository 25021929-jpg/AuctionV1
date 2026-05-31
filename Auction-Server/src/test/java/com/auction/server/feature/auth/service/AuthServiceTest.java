package com.auction.server.feature.auth.service;

import com.auction.server.database.DbExecutor;
import com.auction.server.entity.User;
import com.auction.server.feature.auth.AuthException;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.shared.dto.auth.request.LoginRequest;
import com.auction.shared.dto.auth.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test cho AuthService.
 *
 * Kỹ thuật sử dụng:
 *  - @Mock UserRepository  : mock repository, không cần DB thật
 *  - mockStatic(DbExecutor): bypass transaction, chạy lambda trực tiếp
 *    → cho phép test business logic trong transaction (trùng username, trùng email...)
 *
 * Hai nhóm test:
 *  1. Validate input (trước transaction) — không cần mock DbExecutor
 *  2. Business logic (trong transaction)  — cần mockStatic DbExecutor
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    // Mockito tạo bản giả của UserRepository — không cần DB thật
    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Inject mock repository vào service thay vì HibernateUserRepository thật
        authService = new AuthService(userRepository);
    }

    // =========================================================
    // HELPER
    // =========================================================

    private RegisterRequest validRegisterRequest() {
        return new RegisterRequest(
                "Nguyen Van A",
                "nguyenvana",
                "a@example.com",
                "0901234567",
                "secret123",
                "secret123",
                LocalDate.of(2000, 1, 15)
        );
    }

    /**
     * Tạo DbExecutor mock bypass transaction: chạy Supplier trực tiếp.
     * mockStatic cần được đóng sau mỗi test → dùng try-with-resources.
     */
    private MockedStatic<DbExecutor> mockDbExecutorRunAndReturn() {
        MockedStatic<DbExecutor> dbMock = mockStatic(DbExecutor.class);
        dbMock.when(() -> DbExecutor.runAndReturn(any(Supplier.class)))
                .thenAnswer(inv -> inv.getArgument(0, Supplier.class).get());
        return dbMock;
    }

    private MockedStatic<DbExecutor> mockDbExecutorQuery() {
        MockedStatic<DbExecutor> dbMock = mockStatic(DbExecutor.class);
        dbMock.when(() -> DbExecutor.query(any(Supplier.class)))
                .thenAnswer(inv -> inv.getArgument(0, Supplier.class).get());
        return dbMock;
    }

    private User buildSavedUser(RegisterRequest req) {
        User u = new User();
        u.setId(1L);
        u.setFullName(req.getFullName());
        u.setUsername(req.getUsername().trim());
        u.setEmail(req.getEmail().trim().toLowerCase());
        u.setPhone(req.getPhone());
        u.setDateOfBirth(LocalDate.parse(req.getDateOfBirth()));
        u.setPasswordHash("hashed");
        u.setRole(User.Role.BIDDER);
        u.setIsActive(true);
        return u;
    }

    // =========================================================
    // REGISTER — Validate input (TRƯỚC transaction)
    // Không cần mockStatic vì validate dừng trước khi gọi DbExecutor
    // =========================================================

    @Nested
    @DisplayName("register() — Validate input")
    class RegisterValidation {

        @Test
        @DisplayName("Ném AuthException khi request null")
        void register_nullRequest_throwsAuthException() {
            assertThatThrownBy(() -> authService.register(null))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Invalid request");
        }

        @Test
        @DisplayName("Ném AuthException khi fullName trống")
        void register_blankFullName_throwsAuthException() {
            RegisterRequest req = validRegisterRequest();
            req.setFullName("   ");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Full name required");
        }

        @Test
        @DisplayName("Ném AuthException khi username trống")
        void register_blankUsername_throwsAuthException() {
            RegisterRequest req = validRegisterRequest();
            req.setUsername("");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Username required");
        }

        @Test
        @DisplayName("Ném AuthException khi email trống")
        void register_blankEmail_throwsAuthException() {
            RegisterRequest req = validRegisterRequest();
            req.setEmail(null);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Email required");
        }

        @Test
        @DisplayName("Ném AuthException khi phone trống")
        void register_blankPhone_throwsAuthException() {
            RegisterRequest req = validRegisterRequest();
            req.setPhone("  ");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Phone required");
        }

        @Test
        @DisplayName("Ném AuthException khi dateOfBirth trống")
        void register_blankDateOfBirth_throwsAuthException() {
            RegisterRequest req = validRegisterRequest();
            req.setDateOfBirth("");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Date of birth required");
        }

        @Test
        @DisplayName("Ném AuthException khi password < 6 ký tự")
        void register_shortPassword_throwsAuthException() {
            RegisterRequest req = validRegisterRequest();
            req.setPassword("abc");
            req.setConfirmPassword("abc");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("at least 6 characters");
        }

        @Test
        @DisplayName("Ném AuthException khi password và confirmPassword không khớp")
        void register_passwordMismatch_throwsAuthException() {
            RegisterRequest req = validRegisterRequest();
            req.setPassword("password123");
            req.setConfirmPassword("different456");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Password confirmation does not match");
        }
    }

    // =========================================================
    // REGISTER — Business logic (TRONG transaction)
    // Dùng mockStatic(DbExecutor) để bypass Hibernate
    // Dùng when(userRepository.xxx).thenReturn() để kiểm soát kết quả DB
    // =========================================================

    @Nested
    @DisplayName("register() — Business logic")
    class RegisterBusinessLogic {

        @Test
        @DisplayName("Ném AuthException khi username đã tồn tại")
        void register_duplicateUsername_throwsAuthException() {
            // mockStatic: khi DbExecutor.runAndReturn() được gọi → chạy lambda trực tiếp
            // → userRepository.existsByUsername() thực sự được gọi đến mock
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorRunAndReturn()) {

                // Mock: username đã tồn tại trong DB
                when(userRepository.existsByUsername("nguyenvana")).thenReturn(true);

                RegisterRequest req = validRegisterRequest();
                assertThatThrownBy(() -> authService.register(req))
                        .isInstanceOf(AuthException.class)
                        .hasMessageContaining("trùng tên đăng nhập");

                // Verify: existsByUsername được gọi đúng 1 lần
                verify(userRepository, times(1)).existsByUsername("nguyenvana");
                // Verify: không gọi save vì đã throw trước đó
                verify(userRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Ném AuthException khi email đã tồn tại")
        void register_duplicateEmail_throwsAuthException() {
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorRunAndReturn()) {

                when(userRepository.existsByUsername(anyString())).thenReturn(false);
                // Mock: email đã tồn tại trong DB
                when(userRepository.existsByEmail("a@example.com")).thenReturn(true);

                RegisterRequest req = validRegisterRequest();
                assertThatThrownBy(() -> authService.register(req))
                        .isInstanceOf(AuthException.class)
                        .hasMessageContaining("trùng tên Email");

                verify(userRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Đăng ký thành công — trả về AuthResponse đúng thông tin")
        void register_validRequest_returnsAuthResponse() {
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorRunAndReturn()) {

                // Mock: không trùng username, không trùng email
                when(userRepository.existsByUsername(anyString())).thenReturn(false);
                when(userRepository.existsByEmail(anyString())).thenReturn(false);

                RegisterRequest req = validRegisterRequest();
                User savedUser = buildSavedUser(req);

                // Mock: save() trả về user đã có ID
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                var response = authService.register(req);

                // Verify response không null và đúng thông tin
                assertThat(response).isNotNull();
                assertThat(response.getUser().getUsername()).isEqualTo("nguyenvana");
                assertThat(response.getUser().getEmail()).isEqualTo("a@example.com");

                // Verify: save() được gọi đúng 1 lần
                verify(userRepository, times(1)).save(any(User.class));
            }
        }

        @Test
        @DisplayName("Đăng ký thành công — email được normalize về lowercase")
        void register_emailNormalizedToLowercase() {
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorRunAndReturn()) {

                when(userRepository.existsByUsername(anyString())).thenReturn(false);
                when(userRepository.existsByEmail(anyString())).thenReturn(false);

                RegisterRequest req = validRegisterRequest();
                req.setEmail("User@Example.COM"); // uppercase email

                User savedUser = buildSavedUser(req);
                savedUser.setEmail("user@example.com"); // service phải normalize
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                authService.register(req);

                // Verify: save được gọi với email đã lowercase
                verify(userRepository).save(argThat(u ->
                        u.getEmail().equals("user@example.com")
                ));
            }
        }

        @Test
        @DisplayName("Đăng ký thành công — password được hash, không lưu plaintext")
        void register_passwordIsHashed() {
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorRunAndReturn()) {

                when(userRepository.existsByUsername(anyString())).thenReturn(false);
                when(userRepository.existsByEmail(anyString())).thenReturn(false);

                RegisterRequest req = validRegisterRequest();
                User savedUser = buildSavedUser(req);
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                authService.register(req);

                // Verify: passwordHash lưu vào DB khác với plaintext "secret123"
                verify(userRepository).save(argThat(u ->
                        !u.getPasswordHash().equals("secret123")
                                && u.getPasswordHash().contains(":")  // format salt:hash
                ));
            }
        }
    }

    // =========================================================
    // LOGIN — Validate input (TRƯỚC transaction)
    // =========================================================

    @Nested
    @DisplayName("login() — Validate input")
    class LoginValidation {

        @Test
        @DisplayName("Ném AuthException khi request null")
        void login_nullRequest_throwsAuthException() {
            assertThatThrownBy(() -> authService.login(null))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Invalid request");
        }

        @Test
        @DisplayName("Ném AuthException khi identity trống")
        void login_blankIdentity_throwsAuthException() {
            assertThatThrownBy(() -> authService.login(new LoginRequest("  ", "password")))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Username or email required");
        }

        @Test
        @DisplayName("Ném AuthException khi password trống")
        void login_blankPassword_throwsAuthException() {
            assertThatThrownBy(() -> authService.login(new LoginRequest("nguyenvana", "")))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("Password required");
        }
    }

    // =========================================================
    // LOGIN — Business logic (TRONG transaction)
    // =========================================================

    @Nested
    @DisplayName("login() — Business logic")
    class LoginBusinessLogic {

        @Test
        @DisplayName("Ném AuthException khi username không tồn tại")
        void login_userNotFound_throwsAuthException() {
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorQuery()) {

                // Mock: không tìm thấy user nào
                when(userRepository.findByUsername("nguyenvana")).thenReturn(Optional.empty());
                when(userRepository.findByEmail("nguyenvana")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> authService.login(new LoginRequest("nguyenvana", "secret123")))
                        .isInstanceOf(AuthException.class)
                        .hasMessageContaining("Sai tên đăng nhập hoặc mật khẩu");
            }
        }

        @Test
        @DisplayName("Ném AuthException khi sai password")
        void login_wrongPassword_throwsAuthException() {
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorQuery()) {

                // Tạo user với password đã hash "correctPassword"
                User user = new User();
                user.setId(1L);
                user.setUsername("nguyenvana");
                user.setPasswordHash(
                        com.auction.server.feature.auth.util.PasswordUtil.hashPassword("correctPassword")
                );
                user.setIsActive(true);

                when(userRepository.findByUsername("nguyenvana")).thenReturn(Optional.of(user));

                // Login với password sai
                assertThatThrownBy(() -> authService.login(new LoginRequest("nguyenvana", "wrongPassword")))
                        .isInstanceOf(AuthException.class)
                        .hasMessageContaining("Sai tên đăng nhập hoặc mật khẩu");
            }
        }

        @Test
        @DisplayName("Ném AuthException khi tài khoản bị vô hiệu hóa (isActive = false)")
        void login_inactiveUser_throwsAuthException() {
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorQuery()) {

                User user = new User();
                user.setId(1L);
                user.setUsername("nguyenvana");
                user.setPasswordHash(
                        com.auction.server.feature.auth.util.PasswordUtil.hashPassword("secret123")
                );
                user.setIsActive(false); // tài khoản bị khóa

                when(userRepository.findByUsername("nguyenvana")).thenReturn(Optional.of(user));

                assertThatThrownBy(() -> authService.login(new LoginRequest("nguyenvana", "secret123")))
                        .isInstanceOf(AuthException.class)
                        .hasMessageContaining("Sai tên đăng nhập hoặc mật khẩu");
            }
        }

        @Test
        @DisplayName("Login thành công bằng username — trả về AuthResponse đúng")
        void login_correctUsername_returnsAuthResponse() {
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorQuery()) {

                User user = new User();
                user.setId(1L);
                user.setUsername("nguyenvana");
                user.setEmail("a@example.com");
                user.setFullName("Nguyen Van A");
                user.setPasswordHash(
                        com.auction.server.feature.auth.util.PasswordUtil.hashPassword("secret123")
                );
                user.setRole(User.Role.BIDDER);
                user.setIsActive(true);

                when(userRepository.findByUsername("nguyenvana")).thenReturn(Optional.of(user));

                var response = authService.login(new LoginRequest("nguyenvana", "secret123"));

                assertThat(response).isNotNull();
                assertThat(response.getUser().getUsername()).isEqualTo("nguyenvana");
                assertThat(response.getUser().getEmail()).isEqualTo("a@example.com");
            }
        }

        @Test
        @DisplayName("Login thành công bằng email — tìm qua findByEmail")
        void login_correctEmail_returnsAuthResponse() {
            try (MockedStatic<DbExecutor> dbMock = mockDbExecutorQuery()) {

                User user = new User();
                user.setId(2L);
                user.setUsername("nguyenvana");
                user.setEmail("a@example.com");
                user.setFullName("Nguyen Van A");
                user.setPasswordHash(
                        com.auction.server.feature.auth.util.PasswordUtil.hashPassword("secret123")
                );
                user.setRole(User.Role.BIDDER);
                user.setIsActive(true);

                // Login bằng email: findByUsername trả về empty, findByEmail trả về user
                when(userRepository.findByUsername("a@example.com")).thenReturn(Optional.empty());
                when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(user));

                var response = authService.login(new LoginRequest("a@example.com", "secret123"));

                assertThat(response).isNotNull();
                assertThat(response.getUser().getUsername()).isEqualTo("nguyenvana");
            }
        }
    }
}