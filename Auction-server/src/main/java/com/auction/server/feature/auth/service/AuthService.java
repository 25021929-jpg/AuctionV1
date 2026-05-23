package com.auction.server.feature.auth.service;

import com.auction.server.exception.DataAccessException;
import com.auction.server.feature.auth.AuthException;
import com.auction.server.feature.auth.dto.*;
import com.auction.server.feature.auth.repository.PasswordResetRepository;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.server.feature.auth.util.PasswordUtil;
import com.auction.server.feature.auth.util.ResetTokenUtil;
import com.auction.shared.model.PasswordResetToken;
import com.auction.server.model.User;

import java.time.LocalDateTime;

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.passwordResetRepository =
                new PasswordResetRepository();
    }

    // =====================================================
    // REGISTER
    // =====================================================
    public com.auction.shared.dto.AuthResponse register(
            RegisterRequest request
    ) {

        validateRegister(request);

        // Chuẩn hóa dữ liệu trước khi xử lý
        String fullName =
                request.getFullName().trim();

        String username =
                request.getUsername().trim();

        String email =
                request.getEmail().trim().toLowerCase();

        String phone =
                request.getPhone().trim();

        String dob =
                request.getDateOfBirth().trim();

        String password =
                request.getPassword();

        try {

            // Kiểm tra username đã tồn tại chưa
            if (userRepository.existsByUsername(username)) {

                throw new AuthException(
                        "Username already exists"
                );
            }

            // Kiểm tra email đã tồn tại chưa
            if (userRepository.existsByEmail(email)) {

                throw new AuthException(
                        "Email already exists"
                );
            }

            // Hash password trước khi lưu DB
            String passwordHash =
                    PasswordUtil.hashPassword(password);

            User user = new User(
                    null,
                    fullName,
                    username,
                    email,
                    phone,
                    dob,
                    passwordHash,
                    "BIDDER"
            );

            // Lưu database
            User saved =
                    userRepository.save(user);

            // Convert User -> UserInfo an toàn
            com.auction.shared.dto.UserInfo userInfo =
                    toUserInfo(saved);

            return com.auction.shared.dto.AuthResponse
                    .fromUserInfo(userInfo);

        } catch (DataAccessException e) {

            throw new AuthException(
                    "System error while registering"
            );
        }
    }

    // =====================================================
    // LOGIN
    // =====================================================
    public com.auction.shared.dto.AuthResponse login(
            LoginRequest request
    ) {

        validateLogin(request);

        String loginId =
                request.identity().trim();

        String password =
                request.password();

        try {

            // Tìm bằng username HOẶC email
            User user =
                    userRepository.findByLoginId(
                            loginId
                    );

            // Không nói rõ sai username/email
            if (user == null) {

                throw new AuthException(
                        "Invalid username/email or password"
                );
            }

            // So sánh password thật với hash
            boolean match =
                    PasswordUtil.verifyPassword(
                            password,
                            user.getPasswordHash()
                    );

            if (!match) {

                throw new AuthException(
                        "Invalid username/email or password"
                );
            }

            com.auction.shared.dto.UserInfo userInfo =
                    toUserInfo(user);

            return com.auction.shared.dto.AuthResponse
                    .fromUserInfo(userInfo);

        } catch (DataAccessException e) {

            throw new AuthException(
                    "System error while login"
            );
        }
    }

    // =====================================================
    // FORGOT PASSWORD
    // =====================================================
    public String forgotPassword(ForgotPasswordRequest request) {

        if (request == null || isBlank(request.getEmail())) {
            throw new AuthException("Email required");
        }

        try {
            User user = userRepository.findByEmail(
                    request.getEmail().trim().toLowerCase()
            );

            //Luôn trả cùng 1 message dù email có tồn tại hay không
            if (user == null) {
                return "If your email is registered, you will receive a reset token.";
            }

            String token = ResetTokenUtil.generateToken();
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(15);

            passwordResetRepository.saveToken(user.getId(), token, expiredAt);

            // TODO: gửi token qua email thay vì trả trực tiếp
            return "If your email is registered, you will receive a reset token.";

        } catch (AuthException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new AuthException("System error while resetting password");
        }
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================
    public void resetPassword(
            ResetPasswordRequest request
    ) {

        validateResetPassword(request);

        try {

            PasswordResetToken token =
                    passwordResetRepository
                            .findByToken(
                                    request.getToken()
                            );

            if (token == null) {

                throw new AuthException(
                        "Invalid token"
                );
            }

            // Token đã dùng chưa
            if (token.isUsed()) {

                throw new AuthException(
                        "Token already used"
                );
            }

            // Token hết hạn chưa
            if (token.getExpiredAt()
                    .isBefore(
                            LocalDateTime.now()
                    )) {

                throw new AuthException(
                        "Token expired"
                );
            }

            // Hash password mới
            String newHash =
                    PasswordUtil.hashPassword(
                            request.getNewPassword()
                    );

            // Update password
            userRepository.updatePassword(
                    token.getUserId(),
                    newHash
            );

            // Đánh dấu token đã dùng
            passwordResetRepository.markUsed(
                    token.getId()
            );

        } catch (DataAccessException e) {

            throw new AuthException(
                    "System error while resetting password"
            );
        }
    }

    // =====================================================
    // VALIDATE REGISTER
    // =====================================================
    private void validateRegister(
            RegisterRequest request
    ) {

        if (request == null) {

            throw new AuthException(
                    "Invalid request"
            );
        }

        if (isBlank(request.getFullName())) {

            throw new AuthException(
                    "Full name required"
            );
        }

        if (isBlank(request.getUsername())) {

            throw new AuthException(
                    "Username required"
            );
        }

        if (isBlank(request.getEmail())) {

            throw new AuthException(
                    "Email required"
            );
        }

        if (isBlank(request.getPhone())) {

            throw new AuthException(
                    "Phone required"
            );
        }

        if (isBlank(request.getDateOfBirth())) {

            throw new AuthException(
                    "Date of birth required"
            );
        }

        if (isBlank(request.getPassword())) {

            throw new AuthException(
                    "Password required"
            );
        }

        if (request.getPassword().length() < 6) {

            throw new AuthException(
                    "Password must be at least 6 characters"
            );
        }

        if (!request.getPassword()
                .equals(
                        request.getConfirmPassword()
                )) {

            throw new AuthException(
                    "Password confirmation does not match"
            );
        }
    }

    // =====================================================
    // VALIDATE LOGIN
    // =====================================================
    private void validateLogin(LoginRequest request) {

        if (request == null) {

            throw new AuthException(
                    "Invalid request"
            );
        }

        if (isBlank(request.identity())) {

            throw new AuthException(
                    "Username or email required"
            );
        }

        if (isBlank(request.password())) {

            throw new AuthException(
                    "Password required"
            );
        }
    }

    // =====================================================
    // VALIDATE RESET PASSWORD
    // =====================================================
    private void validateResetPassword(
            ResetPasswordRequest request
    ) {

        if (request == null) {

            throw new AuthException(
                    "Invalid request"
            );
        }

        if (isBlank(request.getToken())) {

            throw new AuthException(
                    "Token required"
            );
        }

        if (isBlank(request.getNewPassword())) {

            throw new AuthException(
                    "New password required"
            );
        }

        if (request.getNewPassword().length() < 6) {

            throw new AuthException(
                    "Password must be at least 6 characters"
            );
        }

        if (!request.getNewPassword()
                .equals(
                        request.getConfirmPassword()
                )) {

            throw new AuthException(
                    "Password confirmation does not match"
            );
        }
    }

    // =====================================================
    // HELPER
    // =====================================================
    private boolean isBlank(String s) {

        return s == null
                || s.trim().isEmpty();
    }
    // Chuyển User -> UserInfo an toàn để trả về client
    private com.auction.shared.dto.UserInfo toUserInfo(User user) {
        if (user == null) return null;
        return new com.auction.shared.dto.UserInfo(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getDateOfBirth(),
                user.getRole()
        );
    }
}
