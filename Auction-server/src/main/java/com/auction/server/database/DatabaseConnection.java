package com.auction.server.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Class dùng chung để tạo kết nối tới MySQL database
public class DatabaseConnection {

    private static final String PROPERTIES_FILE = "application.properties";

    // Hàm tạo kết nối database
    public static Connection getConnection() throws SQLException {
        try {
            Properties properties = new Properties();

            // Đọc file application.properties trong resources
            InputStream inputStream = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream(PROPERTIES_FILE);

            if (inputStream == null) {
                throw new RuntimeException("Cannot find application.properties");
            }

            properties.load(inputStream);

            String dbUrl = properties.getProperty("db.url");
            String dbUsername = properties.getProperty("db.username");
            String dbPassword = properties.getProperty("db.password");

            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

        } catch (Exception e) {
            throw new SQLException("Cannot connect to database", e);
        }
    }
}