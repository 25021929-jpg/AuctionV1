package com.auction.client.feature.auth.controller;


import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.shared.dto.UserInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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

    @FXML private void handleNavHistory() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/auction/client/feature/auction/view/bid-history-view.fxml"));
        Parent root = loader.load();
        BidHistoryController ctrl = loader.getController();
        ctrl.initAuction("001", "Laptop Dell XPS 15", 15_000_000);
        UserInfo currentUser = null;
        ctrl.setCurrentUsername(currentUser.getUsername()); // để tô vàng bid của mình

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
