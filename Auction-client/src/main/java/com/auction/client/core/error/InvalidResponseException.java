package com.auction.client.core.error;

/** Lỗi khi response từ server thiếu hoặc sai định dạng client kỳ vọng. */
public class InvalidResponseException extends ClientException {

    public InvalidResponseException(String message) {
        super(message);
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
