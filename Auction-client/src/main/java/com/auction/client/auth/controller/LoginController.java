package com.auction.client.auth.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @FXML
    public void handleLogin() {
        String username = usernameField.getText(); // Lấy chữ trong ô Username
        String password = passwordField.getText();
        // TODO: gọi service xử lý
    }

    public void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/main/resources/com/aution/client/auth/view/register.fxml")
            );
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 450, 650);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}