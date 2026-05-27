package com.auction.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối MySQL qua HikariCP (một pool dùng chung cho toàn server).
 * <p>
 * Luồng sử dụng:
 * <ol>
 *   <li>{@link #initializePool()} — gọi <b>một lần</b> khi server khởi động (MainServer)</li>
 *   <li>{@link #getConnection()} — repository mượn Connection, trả bằng {@code close()} trong try-with-resources</li>
 *   <li>{@link #shutdownPool()} — gọi khi tắt server (shutdown hook)</li>
 * </ol>
 * {@code conn.close()} trên connection từ pool <b>không</b> đóng TCP — Hikari trả connection về pool
 * (xem tài liệu HikariProxyConnection trong giaithich.md).
 */
public class DatabaseConnection {

    private static final String PROPERTIES_FILE = "application.properties";

    /** Pool singleton — null cho đến khi {@link #initializePool()} chạy xong. */
    // 1. THÊM VOLATILE: Đảm bảo mọi Thread đều nhìn thấy thuộc tính này ngay khi khởi tạo
    private static volatile HikariDataSource dataSource;
    //Helper lấy DataSource để truyền cho HibernateUtil
    public static HikariDataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("Connection Pool chưa khởi tạo");
        }
        return dataSource;
    }
    /**
     * Khởi tạo HikariCP pool từ {@code application.properties} trên classpath.
     * <p>
     * Thread-safe: {@code synchronized} + kiểm tra {@code dataSource != null} tránh tạo hai pool
     * khi nhiều thread gọi cùng lúc lúc startup (pool cũ sẽ leak nếu bị ghi đè).
     */
    public static synchronized void initializePool() {
        if (dataSource != null) {
            return;
        }

        Properties props = loadPropertiesFromClasspath();

        HikariConfig config = new HikariConfig();
        // URL JDBC — timezone/encoding khai báo trong application.properties
        config.setJdbcUrl(requireProperty(props, "db.url"));
        config.setUsername(requireProperty(props, "db.username"));
        config.setPassword(requireProperty(props, "db.password"));

        config.setMaximumPoolSize(parseIntProperty(props, "db.pool.maximumPoolSize"));
        config.setMinimumIdle(parseIntProperty(props, "db.pool.minimumIdle"));
        config.setConnectionTimeout(parseLongProperty(props, "db.pool.connectionTimeout"));
        config.setIdleTimeout(parseLongProperty(props, "db.pool.idleTimeout"));
        config.setMaxLifetime(parseLongProperty(props, "db.pool.maxLifetime"));

        // Validation query khi lấy connection từ pool (MySQL)
//        config.setConnectionTestQuery("SELECT 1");
        // 2. XÓA BỎ setConnectionTestQuery("SELECT 1") để dùng Connection.isValid() mặc định cực nhanh.(của hikariCP, không phải của MySQL driver) — tránh query thừa mỗi lần mượn connection.

        // 3. KÍCH HOẠT VŨ KHÍ BÍ MẬT: Tối ưu hóa tối đa cho MySQL
        config.addDataSourceProperty("cachePrepStmts", "true"); // Bật cache câu lệnh ở phía Client
        config.addDataSourceProperty("prepStmtCacheSize", "250"); // Số lượng câu SQL được cache
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048"); // Giới hạn độ dài câu SQL được cache
        config.addDataSourceProperty("useServerPrepStmts", "true"); // BẮT BUỘC: Ép MySQL biên dịch trước để chống SQL Injection và tăng tốc

        config.setPoolName("AuctionPool");

        // 3. KÍCH HOẠT VŨ KHÍ BÍ MẬT: Tối ưu hóa tối đa cho MySQL
        config.addDataSourceProperty("cachePrepStmts", "true"); // Bật cache câu lệnh ở phía Client
        config.addDataSourceProperty("prepStmtCacheSize", "250"); // Số lượng câu SQL được cache
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048"); // Giới hạn độ dài câu SQL được cache
        config.addDataSourceProperty("useServerPrepStmts", "true"); // BẮT BUỘC: Ép MySQL biên dịch trước để chống SQL Injection và tăng tốc

        dataSource = new HikariDataSource(config);
        System.out.println("HikariCP pool started (AuctionPool)");
    }

    /**
     * Mượn một {@link Connection} từ pool.
     * Bắt buộc đóng trong try-with-resources — Hikari nhận lại connection, không tạo socket mới mỗi lần gọi.
     *
     * @throws SQLException nếu pool chưa được {@link #initializePool()} hoặc DB không khả dụng
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException(
                    "Pool chưa khởi tạo. Gọi DatabaseConnection.initializePool() trước khi dùng repository.");
        }
        return dataSource.getConnection();
    }

    /** Đóng pool khi server dừng — giải phóng socket TCP tới MySQL. */
    public static synchronized void shutdownPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("HikariCP pool closed");
        }
        dataSource = null;
    }

    // ── Đọc config từ classpath (module Auction-Server) ─────────────────────

    /**
     * Đọc {@value #PROPERTIES_FILE} qua ClassLoader của process server.
     * File nằm tại {@code src/main/resources/} → sau build: {@code target/classes/}.
     */
    private static Properties loadPropertiesFromClasspath() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            if (is == null) {
                throw new RuntimeException(
                        "Không tìm thấy " + PROPERTIES_FILE + " trên classpath (kiểm tra src/main/resources)");
            }
            props.load(is);
            return props;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc " + PROPERTIES_FILE, e);
        }
    }

    private static String requireProperty(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Thiếu hoặc rỗng property: " + key);
        }
        return value.trim();
    }

    private static int parseIntProperty(Properties props, String key) {
        try {
            return Integer.parseInt(requireProperty(props, key));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Property không phải số nguyên: " + key, e);
        }
    }

    private static long parseLongProperty(Properties props, String key) {
        try {
            return Long.parseLong(requireProperty(props, key));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Property không phải số: " + key, e);
        }
    }
}
