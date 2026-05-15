package com.auction.client;

import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.network.SocketClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // 1. CUNG CẤP STAGE CHO NAVIGATOR (BẮT BUỘC PHẢI CÓ DÒNG NÀY)
        SceneNavigator.setStage(stage);

        // 2. Load màn hình login đầu tiên
        FXMLLoader fxmlLoader = new FXMLLoader(MainClient.class.getResource("/com/auction/client/feature/auth/view/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();

        // 3. Connect trên background thread
        //    KHÔNG block JavaFX thread
        Thread connectThread = new Thread(() -> {
            try {
                SocketClient.getInstance().connect();
                System.out.println("Đã kết nối đến server!");

                // Có thể notify LoginController enable button ở đây
                Platform.runLater(() -> {
                    // LoginController tự check isConnected()
                    // nên không cần làm gì thêm ở đây
                });

            } catch (IOException e) {
                Platform.runLater(() ->
                        AlertHelper.showError(
                                "Kết nối thất bại",
                                "Không thể kết nối đến server.\nVui lòng kiểm tra lại!"
                        )
                );
            }
        });

        connectThread.setDaemon(true);
        connectThread.start();
    }

    @Override
    public void stop() {
        // App tắt → đóng kết nối
        SocketClient.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}