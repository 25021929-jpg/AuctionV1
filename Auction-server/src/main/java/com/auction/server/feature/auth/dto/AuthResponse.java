package com.auction.server.feature.auth.dto;

// DTO dùng để trả kết quả đăng nhập / đăng ký về cho client
public class AuthResponse {

    private boolean success; // true nếu thành công, false nếu thất bại
    private String message;  // Thông báo kết quả
    private UserInfo user;   // Thông tin user sau khi login/register thành công

    // Constructor rỗng
    public AuthResponse() {
    }

    public AuthResponse(boolean success, String message, UserInfo user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    // Hàm tạo response thành công
    public static AuthResponse success(UserInfo user, String message) {
        return new AuthResponse(true, message, user);
    }

    // Hàm tạo response thất bại
    public static AuthResponse fail(String message) {
        return new AuthResponse(false, message, null);
    }

    // Getter / Setter
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    // Class nhỏ bên trong dùng để gửi thông tin user về client
    // Không gửi passwordHash về client để đảm bảo an toàn
    public static class UserInfo {

        private Long id;
        private String username;
        private String fullName;
        private String role;

        public UserInfo() {
        }

        public UserInfo(Long id, String username, String fullName, String role) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public String getRole() {
            return role;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}