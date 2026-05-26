package com.auction.client.core.error;

import java.io.IOException;

/** Base exception cho các lỗi do client phân loại. */
public class ClientException extends IOException {

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
