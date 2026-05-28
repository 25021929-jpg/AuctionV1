package com.auction.server.network;

import com.auction.shared.dto.Request;
import com.auction.shared.dto.Response;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestDispatcher requestDispatcher;
    private final Gson gson;

    public ClientHandler(Socket socket, RequestDispatcher requestDispatcher) {
        this.socket = socket;
        this.requestDispatcher = requestDispatcher;
        this.gson = new Gson();
    }

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
                    // Nếu lỗi bất ngờ khi xử lý 1 request
                    Response<?> errorResponse =
                            Response.fail("Invalid request");

                    writer.println(gson.toJson(errorResponse));
                }
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());

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
            System.out.println("Error closing client socket");
        }
    }
}