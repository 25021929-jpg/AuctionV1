package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginController {

    @FXML private TextField usernameField; // Khớp fx:id="usernameField"
    @FXML private PasswordField passwordField; // Khớp fx:id="passwordField"
    @FXML private Label errorLabel;

    @FXML
    private ImageView logoImageView;

    //Hàm initialize đề phòng
//    @FXML
//    public void initialize() {
//        // Set ảnh trực tiếp bằng code
//        var url = getClass().getResource("/com/auction/client/css/logo.png");
//        if (url != null) {
//            Image img = new Image(url.toExternalForm());
//            logoImageView.setImage(img);
//            System.out.println("Set image OK, width: " + img.getWidth());
//        }
//    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            // Cách 1: Dùng AlertHelper
            AlertHelper.showError("Lỗi đăng nhập", "Vui lòng nhập đầy đủ thông tin!");

            // Cách 2: Hiển thị ngay trên giao diện qua errorLabel (nếu muốn)
            errorLabel.setText("Tài khoản/Mật khẩu không được để trống");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        // Giả sử đăng nhập thành công
        if (user.equals("admin") && pass.equals("123")) {
            AlertHelper.showInfo("Thành công", "Chào mừng bạn quay trở lại!");
            // Chuyển sang trang chủ
            SceneNavigator.switchScene("/com/auction/client/feature/auth/view/home-view.fxml");
        } else {
            AlertHelper.showError("Thất bại", "Sai tài khoản hoặc mật khẩu!");
        }
    }

    @FXML
    private void handleNavigateRegister(ActionEvent event) {
        // Chuyển sang màn hình đăng ký
        SceneNavigator.switchScene("/com/auction/client/view/register-view.fxml");
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        System.out.println("Quên mật khẩu clicked!");
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/forgot-password-view.fxml");
    }

}