package com.auction.server.feature.auth.service;

import com.auction.server.exception.DataAccessException;
import com.auction.server.feature.auth.AuthException;
import com.auction.server.feature.auth.dto.AuthResponse;
import com.auction.server.feature.auth.dto.LoginRequest;
import com.auction.server.feature.auth.dto.RegisterRequest;
import com.auction.server.feature.auth.repository.UserRepository;
import com.auction.shared.model.User;

// Service xử lý nghiệp vụ đăng nhập / đăng ký
public class AuthService {

    private final UserRepository userRepository;

    // Constructor khởi tạo repository
    public AuthService() {
        this.userRepository = new UserRepository();
    }

    // Xử lý đăng ký tài khoản
    public AuthResponse register(RegisterRequest request) {

        // Kiểm tra dữ liệu đầu vào
        validateRegister(request);

        String username = request.getUsername().trim();
        String password = request.getPassword();
        String fullName = request.getFullName().trim();

        try {
            // Kiểm tra username đã tồn tại chưa
            if (userRepository.existsByUsername(username)) {
                throw new AuthException("Username already exists");
            }

            // Mã hóa mật khẩu trước khi lưu database
            String passwordHash = PasswordUtil.hashPassword(password);

            // Lưu user mới với role mặc định là BIDDER
            User user = userRepository.save(
                    username,
                    passwordHash,
                    fullName,
                    "BIDDER"
            );

            // Trả kết quả thành công
            return AuthResponse.success(toUserInfo(user), "Register success");

        } catch (DataAccessException e) {
            throw new AuthException("System error while registering");
        }
    }

    // Xử lý đăng nhập
    public AuthResponse login(LoginRequest request) {

        // Kiểm tra dữ liệu đầu vào
        validateLogin(request);

        String username = request.getUsername().trim();
        String password = request.getPassword();

        try {
            // Tìm user theo username
            User user = userRepository.findByUsername(username);

            // Nếu không tìm thấy user
            if (user == null) {
                throw new AuthException("Invalid username or password");
            }

            // Kiểm tra password
            boolean isPasswordCorrect = PasswordUtil.checkPassword(
                    password,
                    user.getPasswordHash()
            );

            // Nếu password sai
            if (!isPasswordCorrect) {
                throw new AuthException("Invalid username or password");
            }

            // Trả kết quả đăng nhập thành công
            return AuthResponse.success(toUserInfo(user), "Login success");

        } catch (DataAccessException e) {
            throw new AuthException("System error while logging in");
        }
    }

    // Validate dữ liệu đăng ký
    private void validateRegister(RegisterRequest request) {
        if (request == null) {
            throw new AuthException("Register request is null");
        }

        if (isBlank(request.getUsername())) {
            throw new AuthException("Username is required");
        }

        if (isBlank(request.getPassword())) {
            throw new AuthException("Password is required");
        }

        if (isBlank(request.getFullName())) {
            throw new AuthException("Full name is required");
        }

        if (request.getUsername().trim().length() < 4) {
            throw new AuthException("Username must be at least 4 characters");
        }

        if (request.getPassword().length() < 6) {
            throw new AuthException("Password must be at least 6 characters");
        }
    }

    // Validate dữ liệu đăng nhập
    private void validateLogin(LoginRequest request) {
        if (request == null) {
            throw new AuthException("Login request is null");
        }

        if (isBlank(request.getUsername())) {
            throw new AuthException("Username is required");
        }

        if (isBlank(request.getPassword())) {
            throw new AuthException("Password is required");
        }
    }

    // Kiểm tra chuỗi null hoặc rỗng
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Chuyển User entity/model sang UserInfo để trả về client
    // Không trả passwordHash về client
    private AuthResponse.UserInfo toUserInfo(User user) {
        return new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );
    }
}