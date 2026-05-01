package com.auction.client;

import com.auction.client.core.ui.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.setStage(stage);
        // Use classpath-based resource path (SceneNavigator.class.getResource expects this form)
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/login-view.fxml", "Đăng nhập");
    }

    public static void main(String[] args) {
        launch(args);
    }
}