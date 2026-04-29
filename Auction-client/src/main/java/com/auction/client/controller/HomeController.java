package com.auction.client.controller;

import com.auction.client.util.AlertHelper;
import com.auction.client.util.SceneNavigator;
import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void handleLogout() {
        AlertHelper.showInfo("Thông báo", "Bạn đã đăng xuất.");
        SceneNavigator.switchScene("/com/auction/client/view/login-view.fxml", "Đăng nhập");
    }
}