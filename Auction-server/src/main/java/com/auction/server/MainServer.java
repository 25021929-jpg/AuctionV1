package com.auction.server;

import com.auction.server.database.DatabaseConnection;
import com.auction.server.network.ServerSocketManager;

/**
 * Entry point for Auction Server. Starts the ServerSocketManager on the
 * configured port (default 8888).
 */
public class MainServer {

    public static void main(String[] args) {
        // Pool DB phải có trước khi ClientHandler / repository xử lý request
        DatabaseConnection.initializePool();
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::shutdownPool));

        int port = 8888;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port argument, using default 8888");
            }
        }

        System.out.println("Starting Auction Server on port " + port);
        ServerSocketManager server = new ServerSocketManager(port);
        server.start();
    }
}

