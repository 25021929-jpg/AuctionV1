package com.auction.client.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/main/resources/com/aution/client/auth/view/login.fxml")
        );
        Scene scene = new Scene(loader.load(), 450, 650);
        stage.setScene(scene);
        stage.setTitle("TVP Auction");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}