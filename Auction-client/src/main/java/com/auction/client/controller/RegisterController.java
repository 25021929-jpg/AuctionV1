package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;

    @FXML
    public void handleRegister() {
        // TODO: gọi service xử lý
    }

    @FXML
    public void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/client/view/login.fxml")
            );
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 450, 650);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}