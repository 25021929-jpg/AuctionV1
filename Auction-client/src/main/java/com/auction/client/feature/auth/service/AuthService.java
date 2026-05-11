package com.auction.client.feature.auth.service;

import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.auction.client.feature.auth.dto.request.RegisterRequest;
import com.auction.client.feature.auth.dto.request.ForgotPassworkRequest;

import com.auction.model.User;

/**
 * AuthService định nghĩa "hợp đồng" (contract) cho các thao tác xác thực.
 *
 * Áp dụng nguyên lý:
 *  - Interface Segregation (ISP): chỉ khai báo các method liên quan đến auth.
 *  - Dependency Inversion (DIP): các lớp khác (Controller) phụ thuộc vào
 *    interface này, KHÔNG phụ thuộc vào AuthServiceImpl cụ thể.
 */
public interface AuthService {

    /**
     * Đăng nhập bằng email và password.
     *
     * @param dto dữ liệu đăng nhập (email, password)
     * @return User nếu đăng nhập thành công
     * @throws Exception nếu thông tin sai hoặc lỗi mạng
     */
    User login(LoginRequest dto) throws Exception;

    /**
     * Đăng ký tài khoản mới.
     *
     * @param dto dữ liệu đăng ký (username, email, password, ...)
     * @throws Exception nếu dữ liệu không hợp lệ hoặc lỗi mạng
     */
    void register(RegisterRequest dto) throws Exception;

    /**
     * Quên mật khẩu.
     *
     * @param dto dữ liệu quên mật khẩu (email)
     * @throws Exception nếu dữ liệu không hợp lệ hoặc lỗi mạng
     */
    void register(ForgotPassworkRequest dto) throws Exception;
    /**
     * Đăng xuất người dùng hiện tại.
     */
    void logout();
}
