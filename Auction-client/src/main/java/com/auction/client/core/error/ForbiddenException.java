package com.auction.client.core.error;

/** Lỗi khi user đã đăng nhập nhưng không đủ quyền thực hiện thao tác. */
public class ForbiddenException extends ClientException {

  public ForbiddenException(String message) {
    super(message);
  }
}
