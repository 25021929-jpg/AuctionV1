package com.auction.client.core.error;

import java.io.IOException;

/**
 * Chuyển exception kỹ thuật/nghiệp vụ thành thông báo thân thiện cho UI. Controller chỉ nên gọi
 * class này thay vì tự phân nhánh nhiều loại lỗi.
 */
public final class ErrorHandler {

  private ErrorHandler() {}

  public static String getUserMessage(Throwable error) {
    Throwable t = unwrap(error);

    if (t instanceof ValidationException) {
      return fallback(t.getMessage(), "Dữ liệu nhập không hợp lệ.");
    }
    if (t instanceof UnauthorizedException) {
      return fallback(t.getMessage(), "Bạn cần đăng nhập để tiếp tục.");
    }
    if (t instanceof ForbiddenException) {
      return fallback(t.getMessage(), "Bạn không có quyền thực hiện thao tác này.");
    }
    if (t instanceof RequestTimeoutException) {
      return fallback(t.getMessage(), "Server phản hồi quá lâu. Vui lòng thử lại.");
    }
    if (t instanceof ConnectionException) {
      return fallback(
          t.getMessage(), "Không thể kết nối tới server. Vui lòng kiểm tra server đã chạy chưa.");
    }
    if (t instanceof ServerBusinessException || t instanceof ApiException) {
      return fallback(t.getMessage(), "Yêu cầu không thành công.");
    }
    if (t instanceof IOException) {
      return fallback(t.getMessage(), "Không thể kết nối tới server hoặc kết nối bị gián đoạn.");
    }
    return fallback(
        t == null ? null : t.getMessage(), "Đã xảy ra lỗi không xác định. Vui lòng thử lại.");
  }

  public static Throwable unwrap(Throwable error) {
    Throwable t = error;
    while (t instanceof RuntimeException && t.getCause() != null) {
      t = t.getCause();
    }
    return t;
  }

  private static String fallback(String message, String fallback) {
    return message == null || message.isBlank() ? fallback : message;
  }
}
