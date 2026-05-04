package com.auction.server.database;

import com.auction.server.config.ServerConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConnection {

    private static final Logger log = Logger.getLogger(DatabaseConnection.class.getName());
    private static volatile DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection() {
        ServerConfig cfg = ServerConfig.getInstance();
        HikariConfig hikariCfg = new HikariConfig();

        hikariCfg.setJdbcUrl(cfg.getDbUrl());
        hikariCfg.setUsername(cfg.getDbUser());
        hikariCfg.setPassword(cfg.getDbPassword());
        hikariCfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariCfg.setMinimumIdle(cfg.getDbPoolSize());
        hikariCfg.setMaximumPoolSize(cfg.getDbPoolMax());
        hikariCfg.setConnectionTimeout(30_000);
        hikariCfg.setIdleTimeout(600_000);
        hikariCfg.setMaxLifetime(1_800_000);
        hikariCfg.setConnectionTestQuery("SELECT 1");
        hikariCfg.setPoolName("AuctionPool");
        hikariCfg.addDataSourceProperty("cachePrepStmts", "true");
        hikariCfg.addDataSourceProperty("prepStmtCacheSize", "250");

        try {
            this.dataSource = new HikariDataSource(hikariCfg);
            log.info("✅ HikariCP pool khởi động thành công");
        } catch (Exception e) {
            throw new RuntimeException("❌ Không thể khởi tạo database pool: " + e.getMessage(), e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) instance = new DatabaseConnection();
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("HikariCP pool đã đóng.");
        }
    }

    public boolean isHealthy() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            log.warning("DB health check thất bại: " + e.getMessage());
            return false;
        }
    }
}