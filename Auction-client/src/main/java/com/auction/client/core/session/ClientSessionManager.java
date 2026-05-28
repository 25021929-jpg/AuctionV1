package com.auction.client.core.session;

import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.client.network.SocketClient;

/**
 * Quản lý các thao tác vòng đời session ở phía client.
 *
 * <p>Lưu ý: logout chỉ xóa session và quay về màn đăng nhập. Không tự đóng socket vì
 * socket vẫn có thể dùng lại cho lần đăng nhập tiếp theo, đồng thời tránh phát sinh
 * CONNECTION_LOST giả khi người dùng chủ động đăng xuất.
 */
public final class ClientSessionManager {

    private ClientSessionManager() {
    }

    public static void logoutToLogin() {
        UserSession.getInstance().clear();
        SceneNavigator.switchScene(ScenePaths.LOGIN);
    }

    public static void shutdownApplicationSession() {
        UserSession.getInstance().clear();
        SocketClient.getInstance().disconnect();
    }
}
