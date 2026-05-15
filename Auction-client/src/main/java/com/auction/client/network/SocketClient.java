package com.auction.client.network;

import com.auction.shared.dto.Request;
import com.auction.shared.dto.Response;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient implements ServerCommunicator {

    // ── Singleton ────────────────────────────────────────────────
    private static volatile SocketClient instance;

    private SocketClient() {}

    public static SocketClient getInstance() {

        // Kiểm tra lần 1 — KHÔNG lock
        // 99% trường hợp instance đã tồn tại → return ngay
        if (instance == null) {

            // Chỉ lock khi instance CHƯA tồn tại
            synchronized (SocketClient.class) {

                // Kiểm tra lần 2 — BÊN TRONG lock
                // Vì Thread A và Thread B đều vượt qua check lần 1
                // Thread A lock trước → tạo instance
                // Thread B vào sau → check lại → instance != null → bỏ qua
                if (instance == null) {
                    instance = new SocketClient();
                }
            }
        }
        return instance;
    }

    // ── Config ───────────────────────────────────────────────────
    private static final String HOST           = "localhost";
    private static final int    PORT           = 8888;
    private static final int    CONNECT_TIMEOUT = 5_000;  // 5 giây
    private static final int    READ_TIMEOUT    = 10_000; // 10 giây

    // ── State ────────────────────────────────────────────────────
    private Socket       socket;
    private BufferedReader reader;
    private PrintWriter  writer;
    private final Gson   gson = new Gson();

    // ── Connect / Disconnect ──────────────────────────────────────

    /**
     * Tạo kết nối TCP đến server.
     * Gọi từ background thread — KHÔNG gọi trên JavaFX thread!
     */
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

    /**
     * Gửi request, nhận response.
     * synchronized → chỉ 1 thread gửi tại 1 thời điểm,
     * tránh lẫn lộn bytes giữa các request.
     */
    @Override
    public synchronized <T> Response<T> send(
            String action,
            Object body,
            Class<T> responseType) throws IOException {

        // Guard: chưa kết nối thì báo lỗi ngay
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

        // 5. Parse Response<JsonElement> trước (tránh mất data khi T chưa biết)
        Type rawType = new TypeToken<Response<JsonElement>>() {}.getType();
        Response<JsonElement> raw = gson.fromJson(responseLine, rawType);

        // 6. Parse data thành T cụ thể
        T data = null;
        if (raw.getData() != null) {
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