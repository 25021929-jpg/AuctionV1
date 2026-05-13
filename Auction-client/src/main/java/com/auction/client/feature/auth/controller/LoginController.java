package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.UIAnimations;
import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.auction.client.feature.auth.factory.AuthValidatorFactory;
import com.auction.validation.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

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

    @FXML private CheckBox      rememberCheckBox;
    @FXML private Button        loginBtn;

    // Cần inject VBox chứa form để animate
    // Thêm fx:id="formBox" vào VBox bên phải trong FXML
    @FXML private VBox formBox;

    private final Validator<LoginRequest> validator =
            AuthValidatorFactory.createLoginValidator();

    @FXML
    public void initialize() {
        //Chạy animation cho formBox
        UIAnimations.entrance(formBox);
        setupEnterKeyOnFields();
    }



    // ── Thêm: Focus animation cho field ──────────────────

    private void setupEnterKeyOnFields() {
        // Nhấn Enter ở usernameField → nhảy sang passwordField
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // Nhấn Enter ở passwordField → submit login
        passwordField.setOnAction(e -> handleLogin(null));
    }

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
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/register-view.fxml");
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        System.out.println("Quên mật khẩu clicked!");
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/forgot-password-view.fxml");
    }

}