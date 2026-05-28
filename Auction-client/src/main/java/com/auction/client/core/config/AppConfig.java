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

    /*
     * Client và server thường được chạy cùng một máy khi học/dev, vì vậy mặc định
     * dùng loopback address. Nếu cần kết nối sang máy khác trong LAN, truyền thêm:
     * -Dauction.server.host=<ip-server> -Dauction.server.port=<port>
     */
    public static final String SERVER_HOST = System.getProperty("auction.server.host", "127.0.0.1");
    public static final int SERVER_PORT = intProperty("auction.server.port", 8888);

    public static final int CONNECT_TIMEOUT_MS = 5_000;
    public static final int READ_TIMEOUT_MS = 10_000;

    private static int intProperty(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
