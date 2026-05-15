package com.auction.client.network;

import com.auction.shared.dto.Response;
import java.io.IOException;

public interface ServerCommunicator {

    /**
     * Gửi request lên server, nhận về Response<T>
     *
     * @param action       tên action server xử lý (VD: "AUTH_LOGIN")
     * @param body         object sẽ được serialize thành JSON
     * @param responseType class của T để Gson deserialize
     */
    <T> Response<T> send(String action, Object body, Class<T> responseType)
            throws IOException;

    boolean isConnected();
}