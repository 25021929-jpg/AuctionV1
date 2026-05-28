package com.auction.client.core.error;

/** Lỗi khi server không phản hồi trong thời gian cho phép. */
public class RequestTimeoutException extends ClientException {

    public RequestTimeoutException(String message) {
        super(message);
    }

    public RequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
