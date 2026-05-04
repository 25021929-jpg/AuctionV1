package com.auction.server;

import com.auction.server.config.ServerConfig;
import com.auction.server.database.DatabaseConnection;
import com.auction.server.database.DatabaseInitializer;
import java.util.logging.Logger;

public class MainServer {

    private static final Logger log = Logger.getLogger(MainServer.class.getName());

    public static void main(String[] args) {
        log.info("🚀 Auction Server đang khởi động...");

        ServerConfig config = ServerConfig.getInstance();
        log.info("Port: " + config.getPort() + " | Dev mode: " + config.isDevMode());

        DatabaseConnection db = DatabaseConnection.getInstance();
        if (!db.isHealthy()) {
            log.severe("❌ Không kết nối được database. Dừng lại.");
            System.exit(1);
        }

        DatabaseInitializer.run();

        log.info("✅ Server sẵn sàng trên port " + config.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("🛑 Server đang tắt...");
            DatabaseConnection.getInstance().shutdown();
            log.info("👋 Server đã tắt sạch.");
        }));

        try { Thread.currentThread().join(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}