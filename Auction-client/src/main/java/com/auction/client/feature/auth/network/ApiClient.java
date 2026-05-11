package com.auction.client.feature.auth.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ApiClient chịu trách nhiệm quản lý kết nối Socket đến Server.
 *
 * Áp dụng Singleton Pattern để đảm bảo toàn bộ ứng dụng
 * chỉ dùng MỘT kết nối duy nhất đến server.
 */
public class ApiClient {

    // =========================================================
    // SINGLETON PATTERN
    // =========================================================

    /** Instance duy nhất của ApiClient (volatile để thread-safe). */
    private static volatile ApiClient instance;

    /**
     * Lấy instance duy nhất. Dùng Double-Checked Locking để thread-safe.
     *
     * @return instance ApiClient
     */
    public static ApiClient getInstance() {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient();
                }
            }
        }
        return instance;
    }

    // =========================================================
    // FIELDS
    // =========================================================

    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT  = 9999;

    private Socket             socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    // =========================================================
    // CONSTRUCTOR (private - bắt buộc của Singleton)
    // =========================================================

    /** Không cho phép tạo instance từ bên ngoài. */
    private ApiClient() { }

    // =========================================================
    // KẾT NỐI / NGẮT KẾT NỐI
    // =========================================================

    /**
     * Mở kết nối Socket đến server.
     * Gọi phương thức này trước khi gửi/nhận bất kỳ dữ liệu nào.
     *
     * @throws IOException nếu không thể kết nối đến server
     */
    public void connect() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        // Tạo output stream TRƯỚC để tránh deadlock với server
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in  = new ObjectInputStream(socket.getInputStream());
        System.out.println("[ApiClient] Đã kết nối đến server " + SERVER_HOST + ":" + SERVER_PORT);
    }

    /**
     * Đóng kết nối Socket, giải phóng tài nguyên.
     */
    public void disconnect() {
        try {
            if (in     != null) in.close();
            if (out    != null) out.close();
            if (socket != null) socket.close();
            System.out.println("[ApiClient] Đã ngắt kết nối.");
        } catch (IOException e) {
            System.err.println("[ApiClient] Lỗi khi đóng kết nối: " + e.getMessage());
        }
    }

    // =========================================================
    // GỬI / NHẬN DỮ LIỆU
    // =========================================================

    /**
     * Gửi một object (đã Serializable) đến server.
     *
     * @param object đối tượng cần gửi
     * @throws IOException nếu gửi thất bại
     */
    public void send(Object object) throws IOException {
        if (out == null) {
            throw new IOException("Chưa kết nối đến server. Hãy gọi connect() trước.");
        }
        out.writeObject(object);
        out.flush();
        // Reset để tránh ObjectOutputStream cache object cũ
        out.reset();
    }

    /**
     * Nhận một object từ server (blocking – chờ đến khi có dữ liệu).
     *
     * @return object nhận được
     * @throws IOException            nếu lỗi mạng
     * @throws ClassNotFoundException nếu class của object không tồn tại ở client
     */
    public Object receive() throws IOException, ClassNotFoundException {
        if (in == null) {
            throw new IOException("Chưa kết nối đến server. Hãy gọi connect() trước.");
        }
        return in.readObject();
    }

    // =========================================================
    // TIỆN ÍCH
    // =========================================================

    /**
     * Kiểm tra kết nối còn sống không.
     *
     * @return true nếu socket đang kết nối
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
