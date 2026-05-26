package com.auction.client.feature.auth.controller;

import com.auction.client.core.error.ErrorHandler;
import com.auction.client.core.session.UserSession;
import com.auction.client.core.ui.FormHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.client.core.ui.Toast;
import com.auction.client.core.ui.UIAnimations;
import com.auction.client.feature.auth.factory.AuthValidatorFactory;
import com.auction.client.feature.auth.service.AuthService;
import com.auction.client.feature.auth.service.AuthServiceImpl;
import com.auction.client.network.SocketClient;
import com.auction.shared.domain.UserRole;
import com.auction.shared.dto.AuthResponse;
import com.auction.shared.dto.auth.request.LoginRequest;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoginController {

    @FXML private TextField identityField;
    @FXML private PasswordField passwordField;
    @FXML private VBox formBox;
    @FXML private Label identityError;
    @FXML private Label passwordError;
    @FXML private Button loginButton;

    private final Map<Control, Label> fieldErrorMap = new LinkedHashMap<>();
    private final Map<String, Control> fieldMap = new LinkedHashMap<>();

    private final AuthService authService;
    private final Validator<LoginRequest> validator;

    public LoginController() {
        this(new AuthServiceImpl(), AuthValidatorFactory.createLoginValidator());
    }

    public LoginController(AuthService authService, Validator<LoginRequest> validator) {
        this.authService = authService;
        this.validator = validator;
    }

    @FXML
    public void initialize() {
        UIAnimations.entrance(formBox);
        setupFormHelper();
    }

    private void setupFormHelper() {
        fieldErrorMap.put(identityField, identityError);
        fieldErrorMap.put(passwordField, passwordError);

        fieldMap.put("identity", identityField);
        fieldMap.put("password", passwordField);

        FormHelper.bindClearOnChange(fieldErrorMap);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        FormHelper.clearAll(fieldErrorMap);

        LoginRequest request = buildRequest();
        ValidationResult result = validator.validate(request);
        if (!result.valid()) {
            FormHelper.applyErrors(result, fieldMap, fieldErrorMap);
            return;
        }

        if (!SocketClient.getInstance().isConnected()) {
            showToast("Đang kết nối server, vui lòng thử lại!", Toast.Type.WARNING, 2, null);
            return;
        }

        loginButton.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                AuthResponse response = authService.login(request);
                UserSession.getInstance().start(response);

                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    showToast("✓ Đăng nhập thành công", Toast.Type.SUCCESS, 2, this::navigateByRole);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    showToast(ErrorHandler.getUserMessage(e), Toast.Type.ERROR, 3, null);
                });
            }
        }, "auth-login-thread");

        thread.setDaemon(true);
        thread.start();
    }

    private void navigateByRole() {
        UserRole role = UserSession.getInstance().getRole();
        if (role == UserRole.ADMIN) {
            SceneNavigator.switchScene(ScenePaths.ADMIN_DASHBOARD);
        } else if (role == UserRole.SELLER) {
            SceneNavigator.switchScene(ScenePaths.SELLER_DASHBOARD);
        } else {
            SceneNavigator.switchScene(ScenePaths.AUCTION_LIST);
        }
    }

    @FXML
    private void handleNavigateRegister(ActionEvent event) {
        SceneNavigator.switchScene(ScenePaths.REGISTER);
    }


    private LoginRequest buildRequest() {
        return new LoginRequest(
                identityField.getText() == null ? "" : identityField.getText().trim(),
                passwordField.getText()
        );
    }

    private void showToast(String message, Toast.Type type, int seconds, Runnable onFinished) {
        StackPane root = (StackPane) loginButton.getScene().getRoot();
        Toast.show(root, message, type, seconds, onFinished);
    }
}
