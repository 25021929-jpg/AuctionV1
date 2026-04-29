package com.auction.client;

import com.auction.client.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.setStage(stage);
        SceneNavigator.switchScene("/com/auction/client/view/login-view.fxml", "Đăng nhập");
    }

    public static void main(String[] args) {
        launch(args);
    }
}