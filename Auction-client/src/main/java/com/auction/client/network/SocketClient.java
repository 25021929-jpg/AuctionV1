package com.auction.client.network;

import java.net.*;
import java.io.*;
import com.auction.shared.dto.Request;
import com.auction.shared.dto.Response;
import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class SocketClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private static final Gson gson = new Gson();

    // Kết nối đến server
    // Gọi 1 lần khi app khởi động
    public void connect() throws IOException {
        socket = new Socket(HOST, PORT);

        // Reader để nhận JSON từ server
        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        // Writer để gửi JSON lên server
        // true = autoFlush: tự flush sau mỗi println()
        writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true
        );

        System.out.println("Đã kết nối đến server " + HOST + ":" + PORT);
    }

    // Gửi JSON string, nhận JSON string về
    public String sendRequest(String json) throws IOException {
        writer.println(json);      // gửi 1 dòng JSON lên server
        return reader.readLine();  // đọc 1 dòng JSON từ server về
    }

    public void disconnect() throws IOException {
        if (socket != null) socket.close();
    }


    // Generic method: gửi bất kỳ request nào, nhận về Response<T>
    public <T> Response<T> send(String action, Object requestBody, Class<T> responseType)
            throws IOException {

        // 1. Serialize requestBody thành JSON string
        String bodyJson = gson.toJson(requestBody);

        // 2. Bọc vào Request wrapper
        Request request = new Request(action, bodyJson);

        // 3. Serialize toàn bộ Request thành JSON string
        String requestJson = gson.toJson(request);

        // 4. Gửi đi, nhận về
        writer.println(requestJson);
        String responseJson = reader.readLine();

        // 5. Deserialize Response<T>
        Type type = TypeToken.getParameterized(Response.class, responseType).getType();
        return gson.fromJson(responseJson, type);
    }
}