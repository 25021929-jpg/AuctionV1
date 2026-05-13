package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.FormHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.Toast;
import com.auction.client.core.ui.UIAnimations;
import com.auction.client.feature.auth.dto.request.LoginRequest;
import com.auction.client.feature.auth.factory.AuthValidatorFactory;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoginController {

    @FXML private TextField     identityField;
    @FXML private PasswordField passwordField;
    @FXML private VBox          formBox;

    @FXML private Label identityError;
    @FXML private Label passwordError;
    @FXML private Button loginButton;

    // Map Control → Label
    private final Map<Control, Label> fieldErrorMap = new LinkedHashMap<>();

    // Map tên field (khớp ValidationResult) → Control
    private final Map<String, Control> fieldMap = new LinkedHashMap<>();

    private final Validator<LoginRequest> validator =
            AuthValidatorFactory.createLoginValidator();

    // ── initialize ───────────────────────────────────────────────────────────

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

    // ── handleLogin ──────────────────────────────────────────────────────────

    @FXML
    private void handleLogin(ActionEvent event) {
        FormHelper.clearAll(fieldErrorMap);

        ValidationResult result = validator.validate(buildRequest());

        if (!result.valid()) {
            FormHelper.applyErrors(result, fieldMap, fieldErrorMap);
            return;
        }

        // TODO: Gọi API đăng nhập trước, rồi mới navigate
        // Hợp lệ → hiện toast rồi chuyển màn
        StackPane root = (StackPane) loginButton.getScene().getRoot();
        Toast.show(root, "✓ Đăng nhập thành công", Toast.Type.SUCCESS, 2, this::navigateToMain);
        navigateToMain();
    }

    // ── Helper private ───────────────────────────────────────────────────────

    private LoginRequest buildRequest() {
        return new LoginRequest(
                identityField.getText().trim(),
                passwordField.getText()
        );
    }

    private void navigateToMain() {
        SceneNavigator.switchScene(
                "/com/auction/client/feature/main/view/main-view.fxml"
        );
    }

    @FXML
    private void handleNavigateRegister(ActionEvent actionEvent) {
        SceneNavigator.switchScene(
                "/com/auction/client/feature/auth/view/register-view.fxml"
        );
    }

    @FXML
    private void handleNavigateForgotPassword(ActionEvent actionEvent) {
        SceneNavigator.switchScene(
                "/com/auction/client/feature/auth/view/forgot-password-view.fxml"
        );
    }
}