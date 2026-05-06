package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class RegisterController {

    @FXML
    private void handleBackToLogin() {
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/login-view.fxml");
    }

    public void handleRegister(ActionEvent actionEvent) {
    }

    public void handleNavigateLogin(ActionEvent actionEvent) {
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/login-view.fxml");
    }
}
