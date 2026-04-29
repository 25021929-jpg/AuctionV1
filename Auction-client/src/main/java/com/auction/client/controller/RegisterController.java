package com.auction.client.controller;

import com.auction.client.util.AlertHelper;
import com.auction.client.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private void handleRegister() {
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertHelper.showError("Lỗi", "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            AlertHelper.showError("Lỗi", "Mật khẩu nhập lại không khớp.");
            return;
        }

        AlertHelper.showInfo("Thông báo", "Đăng ký thành công cho tài khoản: " + username);
        SceneNavigator.switchScene("/com/auction/client/view/login-view.fxml", "Đăng nhập");
    }

    @FXML
    private void handleBackToLogin() {
        SceneNavigator.switchScene("/com/auction/client/view/login-view.fxml", "Đăng nhập");
    }
}