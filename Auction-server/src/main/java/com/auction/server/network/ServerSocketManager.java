package com.auction.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSocketManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerSocketManager.class);
    private final int port;

    public ServerSocketManager(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected: " + clientSocket.getRemoteSocketAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            logger.error("Server error: " + e.getMessage());
        }
    }
}