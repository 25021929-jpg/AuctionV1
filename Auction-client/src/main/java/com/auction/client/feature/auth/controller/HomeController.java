package com.auction.client.feature.auth.controller;


import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void handleLogout() {
        AlertHelper.showInfo("Thông báo", "Bạn đã đăng xuất.");
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/login-view.fxml");
    }

    public void handleNavDashboard(ActionEvent actionEvent) {
    }

    public void handleNavAuctions(ActionEvent actionEvent) {

    }

    public void handleNavAssets(ActionEvent actionEvent) {

    }

    public void handleNavHistory(ActionEvent actionEvent) {

    }
}
