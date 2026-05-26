package com.auction.server.network;

import com.auction.shared.dto.Request;
import com.auction.shared.dto.Response;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestDispatcher requestDispatcher;
    private final Gson gson;
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);


    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.requestDispatcher = new RequestDispatcher();
        this.gson = new Gson();
    }

    //Khởi tạo luông đọc ghi dữ liệu
    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                PrintWriter writer = new PrintWriter(
                        socket.getOutputStream(),
                        true
                )
        ) {
            String requestJson;

            // Đọc từng dòng JSON client gửi lên
            while ((requestJson = reader.readLine()) != null) {

                try {
                    // Convert JSON -> Request object
                    Request request = gson.fromJson(requestJson, Request.class);

                    // Gửi request vào dispatcher để xử lý đúng action
                    Response<?> response = requestDispatcher.dispatch(request);

                    // Convert Response object -> JSON
                    String responseJson = gson.toJson(response);

                    // Gửi JSON response về client
                    writer.println(responseJson);

                } catch (Exception e) {
                    logger.error("Lỗi xử lý request từ {}: {}", socket.getRemoteSocketAddress(), e.getMessage(), e);
                    Response<?> errorResponse = Response.fail("Invalid request");
                    writer.println(gson.toJson(errorResponse));
                }
            }

        } catch (IOException e) {
            logger.info("Client disconnected: {}", socket.getRemoteSocketAddress());

        } finally {
            closeSocket();
        }
    }

    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing client socket: {}", socket.getRemoteSocketAddress(), e);
        }
    }
}