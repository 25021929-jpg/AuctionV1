package com.auction.client.core.error;

/** Lỗi nghiệp vụ do server trả về, ví dụ bid thấp hơn giá hiện tại hoặc phiên đã đóng. */
public class ServerBusinessException extends ClientException {

  private final String action;
  private final String errorCode;

  public ServerBusinessException(String action, String message) {
    this(action, message, null, null);
  }

  public ServerBusinessException(String action, String message, String errorCode) {
    this(action, message, errorCode, null);
  }

  public ServerBusinessException(String action, String message, Throwable cause) {
    this(action, message, null, cause);
  }

  public ServerBusinessException(String action, String message, String errorCode, Throwable cause) {
    super(message, cause);
    this.action = action;
    this.errorCode = errorCode;
  }

  public String getAction() {
    return action;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
