package com.auction.server.network;

import com.auction.shared.dto.Response;
import com.auction.shared.protocol.JsonSupport;
import com.auction.shared.protocol.WireMessage;
import com.auction.shared.protocol.WireMessageType;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestDispatcher requestDispatcher;
    private final Gson gson;
    private PrintWriter writer;

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
                PrintWriter socketWriter = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                        true
                )
        ) {
            this.writer = socketWriter;
            String requestJson;

            while ((requestJson = reader.readLine()) != null) {
                WireMessage request = null;
                try {
                    request = gson.fromJson(requestJson, WireMessage.class);
                    Response<?> response = requestDispatcher.dispatch(request, this);
                    send(toWireResponse(request, response));
                } catch (Exception e) {
                    send(toInvalidRequestResponse(request));
                }
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        } finally {
            AuctionRoomRegistry.leaveAll(this);
            writer = null;
            closeSocket();
        }
    }

    public synchronized boolean send(WireMessage message) {
        if (writer == null) {
            return false;
        }
        writer.println(gson.toJson(message));
        return !writer.checkError();
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
