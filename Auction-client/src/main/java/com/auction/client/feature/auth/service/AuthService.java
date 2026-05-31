package com.auction.client.feature.auth.service;

import com.auction.shared.dto.AuthResponse;
import com.auction.shared.dto.auth.request.LoginRequest;
import com.auction.shared.dto.auth.request.RegisterRequest;
import java.io.IOException;

/** Service xử lý các request xác thực phía client. */
public interface AuthService {

  /** Đăng nhập và trả về thông tin user nếu server xác nhận thành công. */
  AuthResponse login(LoginRequest request) throws IOException;

  /** Đăng ký tài khoản mới. Server trả lỗi nghiệp vụ qua IOException/ApiException nếu thất bại. */
  void register(RegisterRequest request) throws IOException;
}
