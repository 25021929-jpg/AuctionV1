package com.auction.client.core.error;

/**
 * Exception đại diện cho lỗi nghiệp vụ trả về từ server.
 *
 * <p>Giữ lại tên ApiException để không phá các service/test cũ, nhưng bản chất đây là
 * ServerBusinessException.
 */
public class ApiException extends ServerBusinessException {

  public ApiException(String action, String message) {
    super(action, message);
  }

  public ApiException(String action, String message, String errorCode) {
    super(action, message, errorCode);
  }

  public ApiException(String action, String message, Throwable cause) {
    super(action, message, cause);
  }
}
