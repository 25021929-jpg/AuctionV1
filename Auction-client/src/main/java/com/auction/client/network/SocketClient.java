package com.auction.client.network;

import com.auction.shared.dto.Request;
import com.auction.shared.dto.Response;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient implements ServerCommunicator {

    // ── Singleton ────────────────────────────────────────────────
    private static volatile SocketClient instance;

    private SocketClient() {}

    public static SocketClient getInstance() {
        if (instance == null) {
            synchronized (SocketClient.class) {
                if (instance == null) {
                    instance = new SocketClient();
                }
            }
        }
        return instance;
    }

    // ── Config ───────────────────────────────────────────────────
    private static final String HOST            = "localhost";
    private static final int    PORT            = 8888;
    private static final int    CONNECT_TIMEOUT = 5_000;  // 5 giây
    private static final int    READ_TIMEOUT    = 10_000; // 10 giây

    // ── State ────────────────────────────────────────────────────
    private Socket         socket;
    private BufferedReader reader;
    private PrintWriter    writer;
    private final Gson     gson = new Gson();

    // ── Connect / Disconnect ──────────────────────────────────────
    public void connect() throws IOException {
        Socket s = new Socket();
        s.connect(new InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT);
        s.setSoTimeout(READ_TIMEOUT);

        this.socket = s;
        this.reader = new BufferedReader(
                new InputStreamReader(s.getInputStream()));
        this.writer = new PrintWriter(
                new OutputStreamWriter(s.getOutputStream()), true);
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    // ── isConnected ───────────────────────────────────────────────
    @Override
    public boolean isConnected() {
        return socket != null
                && !socket.isClosed()
                && socket.isConnected();
    }

    // ── Send ──────────────────────────────────────────────────────
    @Override
    public synchronized <T> Response<T> send(
            String action,
            Object body,
            Class<T> responseType) throws IOException {

        if (!isConnected()) {
            throw new IOException("Chưa kết nối đến server!");
        }

        // 1. Serialize body → JSON string
        String bodyJson = gson.toJson(body);

        // 2. Bọc vào Request wrapper
        Request request = new Request(action, bodyJson);

        // 3. Gửi 1 dòng JSON lên server
        writer.println(gson.toJson(request));

        // 4. Đọc 1 dòng JSON từ server về
        String responseLine = reader.readLine();
        if (responseLine == null) {
            throw new IOException("Server đóng kết nối!");
        }

        // 5. Parse Response<JsonElement> trước
        Type rawType = new TypeToken<Response<JsonElement>>() {}.getType();
        Response<JsonElement> raw = gson.fromJson(responseLine, rawType);

        // 6. Parse data thành T — bỏ qua nếu Void hoặc data null
        T data = null;
        if (raw.getData() != null
                && responseType != Void.class
                && responseType != void.class) {
            data = gson.fromJson(raw.getData(), responseType);
        }

        // 7. Ghép lại thành Response<T>
        Response<T> typed = new Response<>();
        typed.setSuccess(raw.isSuccess());
        typed.setMessage(raw.getMessage());
        typed.setData(data);

        return typed;
    }
}