package com.auction.client.network;

import java.io.*;
import java.net.Socket;

public class SocketClient {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public void connect() {
        try {
            socket = new Socket("localhost", 8080);

            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            writer = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server");

        } catch (IOException e) {
            System.out.println("Cannot connect to server: " + e.getMessage());
        }
    }

    public String sendRequest(String jsonRequest) {
        writer.println(jsonRequest);

        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}