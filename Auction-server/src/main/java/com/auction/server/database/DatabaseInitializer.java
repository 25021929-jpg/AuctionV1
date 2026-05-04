package com.auction.server.database;

import com.auction.server.config.ServerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseInitializer {

    private static final Logger log = Logger.getLogger(DatabaseInitializer.class.getName());

    private DatabaseInitializer() {}

    public static void run() {
        log.info("🗄️  Khởi tạo database...");
        try {
            executeSqlFile("sql/schema.sql");
            log.info("✅ schema.sql chạy thành công");

            if (ServerConfig.getInstance().isDevMode()) {
                executeSqlFile("sql/data.sql");
                log.info("✅ data.sql seed thành công");
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Lỗi khởi tạo database: " + e.getMessage(), e);
        }
    }

    private static void executeSqlFile(String path) throws IOException, SQLException {
        List<String> statements = loadStatements(path);
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            try {
                for (String sql : statements) {
                    if (!sql.isBlank()) stmt.execute(sql);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static List<String> loadStatements(String resourcePath) throws IOException {
        InputStream in = DatabaseInitializer.class
                .getClassLoader().getResourceAsStream(resourcePath);
        if (in == null)
            throw new IOException("Không tìm thấy file: " + resourcePath);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int idx = line.indexOf("--");
                if (idx >= 0) line = line.substring(0, idx);
                sb.append(line).append("\n");
            }
        }

        List<String> result = new ArrayList<>();
        for (String part : sb.toString().split(";")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }
}