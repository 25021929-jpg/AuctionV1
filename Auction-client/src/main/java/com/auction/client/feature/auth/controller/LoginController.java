package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.FormHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.Toast;
import com.auction.client.core.ui.UIAnimations;
import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.auction.client.feature.auth.factory.AuthValidatorFactory;
import com.auction.client.network.ServerCommunicator;
import com.auction.client.network.SocketClient;
import com.auction.shared.dto.AuthResponse;
import com.auction.shared.dto.Response;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoginController {

    // ── FXML ─────────────────────────────────────────────────────
    @FXML private TextField     identityField;
    @FXML private PasswordField passwordField;
    @FXML private VBox          formBox;
    @FXML private Label         identityError;
    @FXML private Label         passwordError;
    @FXML private Button        loginButton;

    // ── Form helpers ──────────────────────────────────────────────
    private final Map<Control, Label>  fieldErrorMap = new LinkedHashMap<>();
    private final Map<String, Control> fieldMap      = new LinkedHashMap<>();

    // ── Dependencies ──────────────────────────────────────────────
    // Dùng Interface → dễ mock khi test
    private final ServerCommunicator        communicator;
    private final Validator<LoginRequest>   validator;

    // Constructor mặc định — JavaFX FXML dùng cái này
    public LoginController() {
        this(
                SocketClient.getInstance(),
                AuthValidatorFactory.createLoginValidator()
        );
    }

    // Constructor cho test — inject từ ngoài vào
    public LoginController(
            ServerCommunicator communicator,
            Validator<LoginRequest> validator) {
        this.communicator = communicator;
        this.validator    = validator;
    }

    // ── initialize ────────────────────────────────────────────────
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

    // ── handleLogin ───────────────────────────────────────────────
    @FXML
    private void handleLogin(ActionEvent event) {

        // 1. Clear lỗi cũ
        FormHelper.clearAll(fieldErrorMap);

        // 2. Build + Validate
        LoginRequest request = buildRequest();
        ValidationResult result = validator.validate(request);
        if (!result.valid()) {
            FormHelper.applyErrors(result, fieldMap, fieldErrorMap);
            return;
        }

        // 3. Kiểm tra đã kết nối server chưa
        if (!SocketClient.getInstance().isConnected()) {
            StackPane root = (StackPane) loginButton.getScene().getRoot();
            Toast.show(root,
                    "Đang kết nối server, vui lòng thử lại!",
                    Toast.Type.WARNING, 2, null);
            return;
        }

        // 4. Disable button tránh bấm nhiều lần
        loginButton.setDisable(true);

        // 5. Gửi request trên background thread
        Thread thread = new Thread(() -> {
            try {
                Response<AuthResponse> response =
                        communicator.send("AUTH_LOGIN", request, AuthResponse.class);

                // 6. Kết quả → quay về JavaFX thread để cập nhật UI
                Platform.runLater(() -> {
                    loginButton.setDisable(false);

                    StackPane root = (StackPane) loginButton.getScene().getRoot();

                    if (response.isSuccess()) {
                        Toast.show(root, "✓ Đăng nhập thành công",
                                Toast.Type.SUCCESS, 2, this::navigateToMain);
                    } else {
                        // Server trả về lỗi nghiệp vụ (sai mật khẩu, không tồn tại...)
                        Toast.show(root, response.getMessage(),
                                Toast.Type.ERROR, 3, null);
                    }
                });

            } catch (IOException e) {
                // Lỗi mạng (mất kết nối, timeout...)
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    StackPane root = (StackPane) loginButton.getScene().getRoot();
                    Toast.show(root, "Lỗi kết nối, vui lòng thử lại!",
                            Toast.Type.ERROR, 3, null);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    // ── Navigate ──────────────────────────────────────────────────
    private void navigateToMain() {
        SceneNavigator.switchScene(
                "/com/auction/client/feature/main/view/main-view.fxml"
        );
    }

    @FXML
    private void handleNavigateRegister(ActionEvent event) {
        SceneNavigator.switchScene(
                "/com/auction/client/feature/auth/view/register-view.fxml"
        );
    }

    @FXML
    private void handleNavigateForgotPassword(ActionEvent event) {
        SceneNavigator.switchScene(
                "/com/auction/client/feature/auth/view/forgot-password-view.fxml"
        );
    }

    // ── Private helpers ───────────────────────────────────────────
    private LoginRequest buildRequest() {
        return new LoginRequest(
                identityField.getText().trim(),
                passwordField.getText()
        );
    }
}