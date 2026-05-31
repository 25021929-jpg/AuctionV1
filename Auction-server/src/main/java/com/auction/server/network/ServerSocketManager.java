package com.auction.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketManager {

  private final int port;
  private final RequestDispatcher requestDispatcher; // Biến lưu trữ dispatcher dùng chung

  public ServerSocketManager(int port, RequestDispatcher requestDispatcher) {
    this.port = port;
    this.requestDispatcher = requestDispatcher;
  }

  public void start() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server started on port " + port);

      while (true) {
        // Đợi Client kết nối (Hàm này block luồng chính cho đến khi có khách)
        Socket clientSocket = serverSocket.accept();
        clientSocket.setTcpNoDelay(true);
        System.out.println("Client connected");

        ClientHandler clientHandler = new ClientHandler(clientSocket, requestDispatcher);
        // Giữ nguyên giải pháp 1 luồng xử lý cho 1 client theo đúng yêu cầu đề bài
        new Thread(clientHandler).start();
      }

    } catch (IOException e) {
      System.out.println("Server error: " + e.getMessage());
    }
  }
}
