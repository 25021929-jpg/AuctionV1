package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.SceneNavigator;
import javafx.fxml.FXML;

public class RegisterController {

    @FXML
    private void handleBackToLogin() {
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/login-view.fxml", "Đăng nhập");
    }
}
