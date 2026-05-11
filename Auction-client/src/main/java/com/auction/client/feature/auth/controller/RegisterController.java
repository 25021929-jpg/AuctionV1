package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.Toast;
import com.auction.client.feature.auth.dto.request.RegisterRequest;
import com.auction.client.feature.auth.factory.AuthValidatorFactory;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegisterController {

    @FXML private TextField fullNameField, usernameField;
    @FXML private TextField emailField, phoneField;
    @FXML private DatePicker dobField;
    @FXML private PasswordField passwordField, confirmPasswordField;

    // Đã xóa errorLabel, messageLabel vì không dùng
    @FXML private Button registerBtn;

    @FXML private Label usernameError, emailError;
    @FXML private Label phoneError, dobError;
    @FXML private Label passwordError, confirmPasswordError;
    @FXML private Label fullNameError;

    private final Validator<RegisterRequest> validator =
            AuthValidatorFactory.createRegisterValidator();

    @FXML
    public void initialize() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy");

        dobField.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? fmt.format(date) : "";
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) return null;
                try {
                    return LocalDate.parse(text.trim(), fmt);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        //Real-time clear error khi user chỉnh sửa
        fullNameField.textProperty().addListener((o, old, val) ->
                clearFieldError(fullNameField, fullNameError));
        usernameField.textProperty().addListener((o, old, val) ->
                clearFieldError(usernameField, usernameError));
        emailField.textProperty().addListener((o, old, val) ->
                clearFieldError(emailField, emailError));
        phoneField.textProperty().addListener((o, old, val) ->
                clearFieldError(phoneField, phoneError));
        passwordField.textProperty().addListener((o, old, val) ->
                clearFieldError(passwordField, passwordError));
        confirmPasswordField.textProperty().addListener((o, old, val) ->
                clearFieldError(confirmPasswordField, confirmPasswordError));
        dobField.valueProperty().addListener((o, old, val) ->
                clearFieldError(dobField, dobError));
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        clearAllErrors();

        // FIX 1: Commit text đang gõ dở trong DatePicker
        commitDatePickerValue();

        RegisterRequest request = new RegisterRequest(
                fullNameField.getText(),
                usernameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                dobField.getValue()
        );

        ValidationResult result = validator.validate(request);

        if (!result.valid()) {
            if (result.hasErrorFor("fullName"))
                showFieldError(fullNameField, fullNameError, result.errorFor("fullName"));
            if (result.hasErrorFor("username"))
                showFieldError(usernameField, usernameError, result.errorFor("username"));
            if (result.hasErrorFor("password"))
                showFieldError(passwordField, passwordError, result.errorFor("password"));
            if (result.hasErrorFor("confirmPassword"))
                showFieldError(confirmPasswordField, confirmPasswordError, result.errorFor("confirmPassword"));
            if (result.hasErrorFor("email"))
                showFieldError(emailField, emailError, result.errorFor("email"));
            if (result.hasErrorFor("phoneNumber"))
                showFieldError(phoneField, phoneError, result.errorFor("phoneNumber"));
            if (result.hasErrorFor("birthDate"))
                showDatePickerError(dobField, dobError, result.errorFor("birthDate")); // FIX 2
            return;
        }

        // TODO: Gọi API đăng ký ở đây trước, rồi mới navigate
        // Hợp lệ → hiện toast rồi chuyển màn
        StackPane root = (StackPane) registerBtn.getScene().getRoot();
        Toast.show(root, "✓ Đăng ký thành công!", Toast.Type.SUCCESS, 2, this::navigateToLogin);
    }

    // FIX 1: Commit giá trị đang gõ dở trong DatePicker
    private void commitDatePickerValue() {
        String text = dobField.getEditor().getText();
        if (text != null && !text.isBlank() && dobField.getValue() == null) {
            LocalDate parsed = dobField.getConverter().fromString(text);
            dobField.setValue(parsed); // có thể null nếu sai format — Validator sẽ bắt
        }
    }

    // FIX 2: Style riêng cho DatePicker — target đúng inner editor
    private void showDatePickerError(DatePicker picker, Label errorLabel, String message) {
        picker.getEditor().setStyle(
                "-fx-border-color: #e05252; -fx-border-width: 1.5px;"
        );
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showFieldError(Control field, Label errorLabel, String message) {
        field.setStyle("-fx-border-color: #e05252; -fx-border-width: 1.5px;");
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearFieldError(Control field, Label errorLabel) {
        field.setStyle("");
        if (field instanceof DatePicker dp) {
            dp.getEditor().setStyle(""); // clear cả inner editor
        }
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void clearAllErrors() {
        clearFieldError(fullNameField, fullNameError);
        clearFieldError(usernameField, usernameError);
        clearFieldError(passwordField, passwordError);
        clearFieldError(confirmPasswordField, confirmPasswordError);
        clearFieldError(emailField, emailError);
        clearFieldError(phoneField, phoneError);
        clearFieldError(dobField, dobError);
    }

    // FIX 4: Đổi tên hàm rõ nghĩa hơn
    private void navigateToLogin() {
        SceneNavigator.switchScene(
                "/com/auction/client/feature/auth/view/login-view.fxml"
        );
    }

    @FXML
    private void handleNavigateLogin(ActionEvent actionEvent) {
        navigateToLogin();
    }
}