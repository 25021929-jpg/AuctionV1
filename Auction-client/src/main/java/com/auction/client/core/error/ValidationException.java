package com.auction.client.core.error;

/** Lỗi dữ liệu nhập từ form trước khi gửi request tới server. */
public class ValidationException extends ClientException {

  public ValidationException(String message) {
    super(message);
  }
}
