package com.auction.client.feature.auth.service;

import com.auction.client.feature.auth.dto.LoginDto;
import com.auction.client.feature.auth.dto.RegisterDto;
import com.auction.client.feature.auth.network.ApiClient;
import com.auction.client.feature.auth.validator.AuthValidator;
import com.auction.model.User;
import com.auction.validation.ValidationResult;

/**
 * AuthServiceImpl là lớp thực thi cụ thể của AuthService.
 *
 * Trách nhiệm (SRP):
 *  1. Validate dữ liệu đầu vào qua AuthValidator.
 *  2. Gửi request đến server qua ApiClient.
 *  3. Xử lý response trả về.
 *
 * Không tự validate, không tự mở socket – delegate cho đúng lớp.
 */
public class AuthServiceImpl implements AuthService {

    // =========================================================
    // DEPENDENCIES (inject qua constructor – tốt hơn new trực tiếp)
    // =========================================================

    private final AuthValidator authValidator;
    private final ApiClient     apiClient;

    /**
     * Constructor injection – dễ test, dễ thay thế implementation.
     *
     * @param authValidator validator cho các form auth
     * @param apiClient     client kết nối socket
     */
    public AuthServiceImpl(AuthValidator authValidator, ApiClient apiClient) {
        this.authValidator = authValidator;
        this.apiClient     = apiClient;
    }

    // =========================================================
    // IMPLEMENT AuthService
    // =========================================================

    /**
     * Đăng nhập:
     *  Bước 1 – Validate DTO.
     *  Bước 2 – Gửi request qua socket.
     *  Bước 3 – Nhận và trả về User.
     */
    @Override
    public User login(LoginDto dto) throws Exception {
        // --- Bước 1: Validate ---
        ValidationResult result = authValidator.validateLogin(dto);
        if (!result.isValid()) {
            // Ném exception với thông báo lỗi đầu tiên tìm được
            throw new IllegalArgumentException(result.getFirstError());
        }

        // --- Bước 2: Gửi request ---
        // Dùng một wrapper request để server biết đây là lệnh gì
        Request request = new Request("LOGIN", dto);
        apiClient.send(request);

        // --- Bước 3: Nhận response ---
        Response response = (Response) apiClient.receive();

        if (!response.isSuccess()) {
            throw new Exception(response.getMessage());
        }

        return (User) response.getData();
    }

    /**
     * Đăng ký:
     *  Bước 1 – Validate DTO.
     *  Bước 2 – Gửi request qua socket.
     *  Bước 3 – Kiểm tra kết quả.
     */
    @Override
    public void register(RegisterDto dto) throws Exception {
        // --- Bước 1: Validate ---
        ValidationResult result = authValidator.validateRegister(dto);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getFirstError());
        }

        // --- Bước 2: Gửi request ---
        Request request = new Request("REGISTER", dto);
        apiClient.send(request);

        // --- Bước 3: Nhận response ---
        Response response = (Response) apiClient.receive();

        if (!response.isSuccess()) {
            throw new Exception(response.getMessage());
        }
    }

    /**
     * Đăng xuất: thông báo server và dọn dẹp session phía client.
     */
    @Override
    public void logout() {
        try {
            Request request = new Request("LOGOUT", null);
            apiClient.send(request);
        } catch (Exception e) {
            System.err.println("[AuthServiceImpl] Logout thất bại: " + e.getMessage());
        } finally {
            apiClient.disconnect();
        }
    }

    // =========================================================
    // INNER CLASSES – Request / Response
    // (Có thể tách ra dto/ nếu project lớn hơn)
    // =========================================================

    /**
     * Request được gửi từ Client → Server.
     * Serializable để truyền qua ObjectOutputStream.
     */
    public static class Request implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private final String command; // Ví dụ: "LOGIN", "REGISTER", "LOGOUT"
        private final Object data;    // DTO kèm theo

        public Request(String command, Object data) {
            this.command = command;
            this.data    = data;
        }

        public String getCommand() { return command; }
        public Object getData()    { return data; }
    }

    /**
     * Response nhận từ Server → Client.
     */
    public static class Response implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private final boolean success;
        private final String  message;
        private final Object  data;    // Ví dụ: đối tượng User sau khi login

        public Response(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data    = data;
        }

        public boolean isSuccess() { return success; }
        public String  getMessage(){ return message; }
        public Object  getData()   { return data; }
    }
}
