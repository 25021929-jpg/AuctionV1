package com.auction.client.core.error;

/** Lỗi khi thao tác cần đăng nhập nhưng chưa có session hợp lệ. */
public class UnauthorizedException extends ClientException {

  public UnauthorizedException(String message) {
    super(message);
  }
}
