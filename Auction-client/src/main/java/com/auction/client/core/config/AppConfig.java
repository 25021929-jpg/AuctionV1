package com.auction.client.core.config;

/**
 * Cấu hình chung cho Client.
 *
 * Lưu ý: Khi đổi IP/PORT server, chỉ sửa tại đây.
 */
public final class AppConfig {

    private AppConfig() {
        // utility
    }

    public static final String SERVER_HOST = "172.20.10.3";
    public static final int SERVER_PORT = 8888;

    public static final int CONNECT_TIMEOUT_MS = 5_000;
    public static final int READ_TIMEOUT_MS = 10_000;
}
