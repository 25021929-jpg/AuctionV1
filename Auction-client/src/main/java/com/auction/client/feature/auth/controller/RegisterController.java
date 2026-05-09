package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class RegisterController {
    @FXML
    private TextField fullNameField, usernameField;
    @FXML
    private TextField emailField, phoneField, dobField;
    @FXML
    private PasswordField passwordField, confirmPasswordField;
    @FXML
    private Label errorLabel, messageLabel;
    @FXML
    private Button registerBtn;
    private void handleBackToLogin() {
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/login-view.fxml");
    }
    @FXML
    private void handleRegister(ActionEvent actionEvent) {

        //Nếu thành công thì trở lại phần đăng nhập
        this.handleBackToLogin();
    }
    @FXML
    private void handleNavigateLogin(ActionEvent actionEvent) {
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/login-view.fxml");
    }
}
