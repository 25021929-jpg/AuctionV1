package com.auction.client.core.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneNavigator {
    private static Stage mainStage;

    // Gọi hàm này ở App khởi đầu để lưu lại Stage chính
    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    public static void switchScene(String fxmlPath) {
        try {
            // Quan trọng: getResource tìm file trong thư mục resources
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.centerOnScreen();
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Lỗi hệ thống", "Không thể tải giao diện: " + fxmlPath);
        }
    }
}