package com.auction.client.core.error;

/** Lỗi khi client chưa kết nối được hoặc mất kết nối tới server. */
public class ConnectionException extends ClientException {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
