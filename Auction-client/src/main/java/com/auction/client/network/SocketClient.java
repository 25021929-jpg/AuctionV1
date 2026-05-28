package com.auction.client.network;

import com.auction.client.core.error.ConnectionException;
import com.auction.client.core.error.InvalidResponseException;
import com.auction.client.core.error.RequestTimeoutException;
import com.auction.client.core.event.AppEvent;
import com.auction.client.core.event.EventBus;
import com.auction.client.core.event.EventType;
import com.auction.client.core.event.NetworkEventPayload;
import com.auction.client.core.event.ServerEventMapper;
import com.auction.shared.dto.Response;
import com.auction.shared.protocol.JsonSupport;
import com.auction.shared.protocol.WireMessage;
import com.auction.shared.protocol.WireMessageType;
import com.google.gson.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

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
    private static final String HOST            = com.auction.client.core.config.AppConfig.SERVER_HOST;
    private static final int    PORT            = com.auction.client.core.config.AppConfig.SERVER_PORT;
    private static final int    CONNECT_TIMEOUT = com.auction.client.core.config.AppConfig.CONNECT_TIMEOUT_MS;  // 5 giây
    private static final int    READ_TIMEOUT    = com.auction.client.core.config.AppConfig.READ_TIMEOUT_MS; // 10 giây

    // ── State ────────────────────────────────────────────────────
    private Socket         socket;
    private BufferedReader reader;
    private PrintWriter    writer;
    private final Gson     gson = JsonSupport.createGson();

    /** Thread đọc message từ server (RESPONSE/EVENT). */
    private ExecutorService readerExecutor;

    /** Pending map để match RESPONSE theo requestId. */
    private final Map<String, CompletableFuture<WireMessage>> pending = new ConcurrentHashMap<>();

    // ── Connect / Disconnect ──────────────────────────────────────
    public synchronized void connect() throws IOException {
        if (isConnected()) {
            return;
        }

        disconnect();

        Socket s = new Socket();
        s.connect(new InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT);
        // Không set SO_TIMEOUT cho reader realtime. Nếu set 10s, readLoop sẽ tự báo mất kết nối
        // dù server vẫn còn sống nhưng tạm thời chưa gửi message nào.

        this.socket = s;
        this.reader = new BufferedReader(
                new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(
                new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);

        // Start background reader
        this.readerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "socket-client-reader");
            t.setDaemon(true);
            return t;
        });
        this.readerExecutor.submit(this::readLoop);
    }

    public synchronized void disconnect() {
        closeQuietly(reader);
        if (writer != null) {
            writer.close();
            writer = null;
        }
        closeQuietly(socket);
        reader = null;
        socket = null;

        if (readerExecutor != null) {
            readerExecutor.shutdownNow();
            readerExecutor = null;
        }

        // Fail all pending futures để tránh treo UI.
        for (CompletableFuture<WireMessage> f : pending.values()) {
            f.completeExceptionally(new IOException("Disconnected"));
        }
        pending.clear();
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (IOException ignored) {
        }
    }

    private static void closeQuietly(Socket socket) {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
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
            try {
                /*
                 * MainClient có connect nền lúc mở app, nhưng nếu lúc đó server chưa chạy
                 * hoặc mạng vừa rớt thì request đầu tiên không nên thất bại ngay. Thử nối
                 * lại tại đây giúp mọi service dùng SocketClient có cùng hành vi retry.
                 */
                connect();
            } catch (IOException e) {
                throw new ConnectionException(
                        "Không thể kết nối đến server "
                                + HOST + ":" + PORT
                                + ". Hãy kiểm tra SocketServer đã chạy đúng port chưa.",
                        e
                );
            }
        }

        // PHƯƠNG ÁN 1: 1 line JSON, phân biệt RESPONSE/EVENT bằng field "type".
        // Để tránh "đoán" protocol cũ, client sẽ gửi WireMessage(REQUEST) có requestId.

        final String requestId = UUID.randomUUID().toString();

        // 1) Build wire request
        WireMessage req = new WireMessage();
        req.setType(WireMessageType.REQUEST);
        req.setRequestId(requestId);
        req.setAction(action);
        req.setData(gson.toJsonTree(body));

        // 2) Tạo future chờ RESPONSE
        CompletableFuture<WireMessage> future = new CompletableFuture<>();
        pending.put(requestId, future);

        // 3) Gửi 1 dòng JSON
        writer.println(gson.toJson(req));
        if (writer.checkError()) {
            pending.remove(requestId);
            throw new ConnectionException("Không gửi được request đến server.");
        }

        // 4) Đợi RESPONSE (timeout)
        WireMessage wireResp;
        try {
            wireResp = future.get(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            pending.remove(requestId);
            throw new RequestTimeoutException("Server phản hồi quá lâu. Vui lòng thử lại.", e);
        } catch (ExecutionException e) {
            pending.remove(requestId);
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ConnectionException("Lỗi khi chờ phản hồi từ server.", cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pending.remove(requestId);
            throw new RequestTimeoutException("Yêu cầu bị gián đoạn khi đang chờ server.", e);
        }

        // 5) Convert wire response -> Response<T>
        if (wireResp == null) {
            throw new InvalidResponseException("Server trả về response rỗng.");
        }
        Response<T> typed = new Response<>();
        typed.setSuccess(wireResp.isSuccess());
        typed.setMessage(wireResp.getMessage());
        typed.setErrorCode(wireResp.getErrorCode());

        T data = null;
        if (wireResp.getData() != null
                && responseType != Void.class
                && responseType != void.class) {
            data = gson.fromJson(wireResp.getData(), responseType);
        }
        typed.setData(data);
        return typed;
    }

    /**
     * Background read loop: nhận message và route theo type.
     * - RESPONSE: complete pending future theo requestId
     * - EVENT: publish lên EventBus (không polling) theo yêu cầu đề bài. 
     */
    private void readLoop() {
        try {
            while (isConnected()) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Server đóng kết nối");
                }

                WireMessage msg = gson.fromJson(line, WireMessage.class);
                if (msg == null || msg.getType() == null) {
                    continue;
                }

                if (msg.getType() == WireMessageType.RESPONSE) {
                    handleResponse(msg);
                } else if (msg.getType() == WireMessageType.EVENT) {
                    handleEvent(msg);
                }
            }
        } catch (Exception e) {
            // Fail all pending to avoid deadlocks
            for (CompletableFuture<WireMessage> f : pending.values()) {
                f.completeExceptionally(e);
            }
            pending.clear();

            // Notify UI: mất kết nối (event-based, không polling)
            try {
                EventBus.getInstance().publish(
                        new AppEvent(EventType.CONNECTION_LOST, new NetworkEventPayload("CONNECTION_LOST", null))
                );
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    private void handleResponse(WireMessage msg) {
        if (msg.getRequestId() == null) {
            return;
        }
        CompletableFuture<WireMessage> f = pending.remove(msg.getRequestId());
        if (f != null) {
            f.complete(msg);
        }
    }

    private void handleEvent(WireMessage msg) {
        // Map action -> EventType để các màn hình subscribe rõ ràng.
        // Nếu server đổi tên action, chỉ sửa tại shared ActionConstants + ServerEventMapper.
        EventType type = ServerEventMapper.map(msg.getAction());
        NetworkEventPayload payload = new NetworkEventPayload(msg.getAction(), msg.getData());
        EventBus.getInstance().publish(new AppEvent(type, payload));
    }
}
