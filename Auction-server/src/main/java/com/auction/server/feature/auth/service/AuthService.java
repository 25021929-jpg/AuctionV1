package com.auction.server.feature.auth.service;

import com.auction.server.database.DbExecutor;
import com.auction.server.database.HibernateUtil;
import com.auction.server.feature.auth.AuthException;
import com.auction.server.feature.auth.repository.HibernateUserRepository;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.auth.util.PasswordUtil;
import com.auction.shared.dto.AuthResponse;
import com.auction.shared.dto.UserInfo;
import com.auction.shared.dto.auth.request.LoginRequest;
import com.auction.shared.dto.auth.request.RegisterRequest;
import com.auction.server.entity.User;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * AuthService: xử lý nghiệp vụ đăng ký và đăng nhập.
 *
 * Nguyên tắc thiết kế:
 *   - Không biết về Session/Transaction — DbExecutor lo phần đó
 *   - Không lộ passwordHash ra ngoài — dùng UserInfo DTO
 *   - Không tiết lộ thông tin nhạy cảm trong message lỗi
 *   - Validate format trước khi mở transaction
 *     → tránh tốn DB connection khi input sai
 *
 * Tối ưu connection pool:
 *   - Bcrypt (~100ms CPU) được đưa RA NGOÀI transaction
 *   - DB connection chỉ bị giữ khi thực sự cần query (~2-4ms)
 */
public class AuthService {

    private final UserRepository userRepository;

    // ─── Constructor chính: inject dependency (DIP)
    // Dùng khi test: truyền InMemoryUserRepository để mock
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ─── Constructor tiện lợi: dùng khi production
    // RequestDispatcher gọi new AuthService()
    public AuthService() {
        this(new HibernateUserRepository(HibernateUtil.getSessionFactory()));
    }

    // =====================================================
    // REGISTER (ĐĂNG KÝ)
    // =====================================================

    /**
     * Luồng xử lý nguyên tử (Atomic Register):
     * 1. Validate format & Chuẩn hóa (Ngoài TX)
     * 2. Hash password thô (Ngoài TX - Tốn 100ms CPU nhưng Connection Pool rảnh hoàn toàn)
     * 3. [TX Duy Nhất] Kiểm tra trùng + Lưu DB (Connection giữ ~2-3ms rồi đóng ngay)
     */
    public AuthResponse register(RegisterRequest request) {

        // Bước 1: Validate format ngoài transaction
        validateRegister(request);

        // Bước 2: Chuẩn hóa dữ liệu ngoài transaction
        final String fullName = request.getFullName().trim();
        final String username = request.getUsername().trim();
        final String email    = request.getEmail().trim().toLowerCase();
        final String phone    = request.getPhone().trim();
        final String password = request.getPassword();
        final LocalDate dob   = LocalDate.parse(request.getDateOfBirth().trim());
        final User.Role role   = parseRegisterRole(request.getRole());

        // Bước 3: Hash mật khẩu TRƯỚC KHI mở Transaction
        // Chấp nhận tốn 100ms CPU ngay cả khi trùng tài khoản, đổi lại an toàn tuyệt đối
        final String passwordHash = PasswordUtil.hashPassword(password);

        // Bước 4: Mở 1 Transaction duy nhất: Vừa check trùng vừa ghi dữ liệu
        // Triệt tiêu hoàn toàn khoảng trống Race Condition giữa 2 TX độc lập
        return DbExecutor.runAndReturn(() -> {

            if (userRepository.existsByUsername(username)) {
                throw new AuthException("Lỗi trùng tên đăng nhập");
            }
            if (userRepository.existsByEmail(email)) {
                throw new AuthException("Lỗi trùng tên Email");
            }

            User user = new User();
            user.setFullName(fullName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPhone(phone);
            user.setDateOfBirth(dob);
            user.setPasswordHash(passwordHash);
            user.setRole(role);
            user.setBalance(BigDecimal.ZERO);

            User saved = userRepository.save(user);
            return AuthResponse.fromUserInfo(toUserInfo(saved));
        });
    }

    // =====================================================
    // LOGIN
    // =====================================================

    /**
     * Đăng nhập bằng username HOẶC email.
     *
     * Luồng (tối ưu connection pool — hot path):
     *   1. validate format     — ngoài transaction
     *   2. [TX read-only] tìm user — connection giữ ~1ms rồi trả về pool
     *   3. verifyPassword()    — NGOÀI transaction, CPU ~100ms, DB connection rảnh
     *
     * Bảo mật:
     *   - Không phân biệt "sai username" hay "sai password"
     *     → chống Account Enumeration Attack
     *
     * @throws AuthException nếu sai thông tin đăng nhập
     */
    public AuthResponse login(LoginRequest request) {

        // Validate format — không cần DB connection
        validateLogin(request);

        final String loginId = request.identity().trim();
        final String emailLoginId = loginId.toLowerCase();

        // [TX read-only] chỉ query — DbExecutor.query() tắt dirty checking
        // connection giữ ~1ms rồi trả về pool ngay
        User user = DbExecutor.query(() ->
                userRepository.findByUsername(loginId)
                        // Email đã được lưu lowercase khi đăng ký, nên login bằng email cũng normalize.
                        .or(() -> userRepository.findByEmail(emailLoginId))
                        .orElse(null)
        );

        // verifyPassword NGOÀI transaction — CPU ~100ms
        // DB connection đã rảnh, phục vụ request khác
        //
        // Không nói rõ "sai username" hay "sai password" — bảo mật hơn
        if (user == null
                || Boolean.FALSE.equals(user.getIsActive())
                || !PasswordUtil.verifyPassword(request.password(), user.getPasswordHash())) {
            throw new AuthException("Sai tên đăng nhập hoặc mật khẩu");
        }

        // Trả UserInfo — không có passwordHash
        return AuthResponse.fromUserInfo(toUserInfo(user));
    }

    // =====================================================
    // PRIVATE — VALIDATE
    // =====================================================

    /**
     * Validate format request đăng ký.
     * Gọi TRƯỚC transaction — không tốn DB connection nếu input sai.
     */
    private void validateRegister(RegisterRequest request) {
        if (request == null) {
            throw new AuthException("Invalid request");
        }
        if (isBlank(request.getFullName())) {
            throw new AuthException("Full name required");
        }
        if (isBlank(request.getUsername())) {
            throw new AuthException("Username required");
        }
        if (isBlank(request.getEmail())) {
            throw new AuthException("Email required");
        }
        if (isBlank(request.getPhone())) {
            throw new AuthException("Phone required");
        }
        if (isBlank(request.getDateOfBirth())) {
            throw new AuthException("Date of birth required");
        }
        if (isBlank(request.getPassword())) {
            throw new AuthException("Password required");
        }
        if (request.getPassword().length() < 6) {
            throw new AuthException("Password must be at least 6 characters");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthException("Password confirmation does not match");
        }
        parseRegisterRole(request.getRole());
    }


    /**
     * Chỉ cho phép người dùng tự đăng ký vai trò nghiệp vụ thông thường.
     * ADMIN không được tạo từ form đăng ký public.
     */
    private User.Role parseRegisterRole(String rawRole) {
        if (isBlank(rawRole)) {
            return User.Role.BIDDER;
        }
        try {
            User.Role parsed = User.Role.valueOf(rawRole.trim().toUpperCase());
            if (parsed == User.Role.ADMIN) {
                throw new AuthException("Không thể tự đăng ký tài khoản ADMIN");
            }
            return parsed;
        } catch (IllegalArgumentException ex) {
            throw new AuthException("Role đăng ký không hợp lệ");
        }
    }

    /** Validate format request đăng nhập. */
    private void validateLogin(LoginRequest request) {
        if (request == null) {
            throw new AuthException("Invalid request");
        }
        if (isBlank(request.identity())) {
            throw new AuthException("Username or email required");
        }
        if (isBlank(request.password())) {
            throw new AuthException("Password required");
        }
    }

    // =====================================================
    // PRIVATE — HELPER
    // =====================================================

    /** Kiểm tra chuỗi null hoặc rỗng sau khi trim */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Convert User entity → UserInfo DTO an toàn để trả về client.
     * User entity chứa passwordHash — không bao giờ được ra ngoài Service.
     */
    private UserInfo toUserInfo(User user) {
        if (user == null) return null;
        return new UserInfo(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null,
                user.getRole() != null ? user.getRole().name() : null,
                user.getBalance()
        );
    }
}
