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
    @FXML
    private void handleNavAuctions() {
        System.out.println("Go to auctions");
    }

    @FXML
    private void handleNavCreateAuction() {
        System.out.println("Go to create auction");
    }

    @FXML
    private void handleNavProfile() {
        System.out.println("Go to profile");
    }

    public void handleNavAssets(ActionEvent actionEvent) {

    }

    public void handleNavHistory(ActionEvent actionEvent) {

    }

    public void handleNavMyAssets(ActionEvent actionEvent) {

    }

    public void handleNavWallet(ActionEvent actionEvent) {
    }

    public void handleNavSettings(ActionEvent actionEvent) {

    }

    public void handleViewAllAuctions(ActionEvent actionEvent) {

    }

    public void handleRegisterAsset(ActionEvent actionEvent) {

    }

    public void handleTopUpWallet(ActionEvent actionEvent) {

    }

    public void handleJoinAuction(ActionEvent actionEvent) {
    }
}
