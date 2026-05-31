package com.auction.client.core.error;

import com.auction.shared.dto.Response;

/**
 * Helper xử lý Response trả về từ server.
 *
 * <p>Mục tiêu: tập trung xử lý lỗi ở 1 chỗ, controller/service chỉ cần gọi unwrap(). Tránh rải
 * if/else khắp nơi và giảm rủi ro sửa code cũ.
 */
public final class ResponseUtils {

  private ResponseUtils() {}

  /** Nếu response success -> trả data; ngược lại -> throw lỗi đã phân loại. */
  public static <T> T unwrap(String action, Response<T> response) throws ApiException {
    if (response == null) {
      throw new ApiException(action, "Không nhận được phản hồi từ server (" + action + ")");
    }
    if (!response.isSuccess()) {
      String msg = response.getMessage();
      if (msg == null || msg.isBlank()) {
        msg = "Yêu cầu thất bại (" + action + ")";
      }
      throw new ApiException(action, msg, response.getErrorCode());
    }
    return response.getData();
  }
}
