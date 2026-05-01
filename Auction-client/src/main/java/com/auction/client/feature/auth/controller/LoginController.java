package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertHelper.showError("Lỗi", "Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
            return;
        }

        AlertHelper.showInfo("Thông báo", "Đăng nhập thành công với tài khoản: " + username);
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/home-view.fxml", "Trang chủ");
    }

    @FXML
    private void handleGoToRegister() {
        SceneNavigator.switchScene("/com/auction/client/view/register-view.fxml", "Đăng ký");
    }
}