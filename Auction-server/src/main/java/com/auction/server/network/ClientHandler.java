package com.auction.server.network;

import com.auction.shared.dto.Response;
import com.auction.shared.protocol.JsonSupport;
import com.auction.shared.protocol.WireMessage;
import com.auction.shared.protocol.WireMessageType;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestDispatcher requestDispatcher;
    private final Gson gson;

    public ClientHandler(Socket socket, RequestDispatcher requestDispatcher) {
        this.socket = socket;
        this.requestDispatcher = requestDispatcher;
        this.gson = JsonSupport.createGson();
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                );

                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                        true
                )
        ) {
            String requestJson;

            // Đọc từng dòng JSON client gửi lên
            while ((requestJson = reader.readLine()) != null) {

                WireMessage request = null;
                try {
                    // Convert JSON -> WireMessage object
                    request = gson.fromJson(requestJson, WireMessage.class);

                    // Gửi request vào dispatcher để xử lý đúng action
                    Response<?> response = requestDispatcher.dispatch(request);

                    // Convert Response object -> WireMessage JSON
                    String responseJson = gson.toJson(toWireResponse(request, response));

                    // Gửi JSON response về client
                    writer.println(responseJson);

                } catch (Exception e) {
                    // Nếu lỗi bất ngờ khi xử lý 1 request
                    writer.println(gson.toJson(toInvalidRequestResponse(request)));
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

    private WireMessage toWireResponse(WireMessage request, Response<?> response) {
        WireMessage message = new WireMessage();
        message.setType(WireMessageType.RESPONSE);
        if (request != null) {
            message.setRequestId(request.getRequestId());
            message.setAction(request.getAction());
        }

        if (response == null) {
            message.setSuccess(false);
            message.setMessage("Empty response");
            return message;
        }

        message.setSuccess(response.isSuccess());
        message.setMessage(response.getMessage());
        message.setErrorCode(response.getErrorCode());

        JsonElement data = gson.toJsonTree(response.getData());
        message.setData(data);
        return message;
    }

    private WireMessage toInvalidRequestResponse(WireMessage request) {
        WireMessage message = new WireMessage();
        message.setType(WireMessageType.RESPONSE);
        if (request != null) {
            message.setRequestId(request.getRequestId());
            message.setAction(request.getAction());
        }
        message.setSuccess(false);
        message.setMessage("Invalid request");
        return message;
    }
}
