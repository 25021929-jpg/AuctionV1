package com.auction.client;

import com.auction.client.core.ui.SceneNavigator;
import javafx.application.Application;
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
    }
}