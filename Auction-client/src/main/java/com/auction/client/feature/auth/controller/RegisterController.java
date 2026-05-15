package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.FormHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.Toast;
import com.auction.client.core.ui.UIAnimations;
import com.auction.client.feature.auth.dto.request.RegisterRequest;
import com.auction.client.feature.auth.factory.AuthValidatorFactory;
import com.auction.client.feature.auth.service.AuthService;
import com.auction.shared.dto.Response;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegisterController {

    @FXML private TextField     fullNameField, usernameField;
    @FXML private TextField     emailField, phoneField;
    @FXML private DatePicker    dobField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Button        registerBtn;
    @FXML private VBox          formBox;

    @FXML private Label fullNameError, usernameError;
    @FXML private Label emailError,    phoneError;
    @FXML private Label passwordError, confirmPasswordError;
    @FXML private Label dobError;

    private final AuthService authService = new AuthService();

    // Map Control → Label (thứ tự hiển thị lỗi từ trên xuống)
    private final Map<Control, Label> fieldErrorMap = new LinkedHashMap<>();

    // Map tên field (khớp ValidationResult) → Control
    private final Map<String, Control> fieldMap = new LinkedHashMap<>();

    private final Validator<RegisterRequest> validator =
            AuthValidatorFactory.createRegisterValidator();

    @FXML
    public void initialize() {
        UIAnimations.entrance(formBox); //Chạy animation
        setupDatePicker();
        setupFormHelper();

    }
    //Cài đặt datePicker dobField
    private void setupDatePicker() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy");
        dobField.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? fmt.format(date) : "";
            }
            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) return null;
                try { return LocalDate.parse(text.trim(), fmt); }
                catch (Exception e) { return null; }
            }
        });
    }
    private void setupFormHelper() {
        // Đăng ký cặp field ↔ errorLabel
        fieldErrorMap.put(usernameField, usernameError);
        fieldErrorMap.put(fullNameField, fullNameError);
        fieldErrorMap.put(emailField, emailError);
        fieldErrorMap.put(phoneField, phoneError);
        fieldErrorMap.put(passwordField, passwordError);
        fieldErrorMap.put(confirmPasswordField, confirmPasswordError);
        fieldErrorMap.put(dobField, dobError);

        // Đăng ký tên field khớp với ValidationResult
        fieldMap.put("fullName",        fullNameField);
        fieldMap.put("username",        usernameField);
        fieldMap.put("email",           emailField);
        fieldMap.put("phoneNumber",     phoneField);
        fieldMap.put("password",        passwordField);
        fieldMap.put("confirmPassword", confirmPasswordField);
        fieldMap.put("birthDate",       dobField);

        // Tự clear lỗi khi user chỉnh sửa
        FormHelper.bindClearOnChange(fieldErrorMap);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        FormHelper.clearAll(fieldErrorMap);
        // FIX 1: Commit text đang gõ dở trong DatePicker
        commitDatePickerValue();
        RegisterRequest request = buildRequest();
        ValidationResult result = validator.validate(request);

        if (!result.valid()) {
            FormHelper.applyErrors(result, fieldMap, fieldErrorMap);
            return;
        }
        Response<?> response = authService.register(request);

        if (!response.isSuccess()) {
            emailError.setText(response.getMessage());
            emailError.setVisible(true);
            emailError.setManaged(true);
            return;
        }
        // TODO: Gọi API đăng ký ở đây trước, rồi mới navigate
        // Hợp lệ → hiện toast rồi chuyển màn
        StackPane root = (StackPane) registerBtn.getScene().getRoot();
        Toast.show(root, "✓ Đăng ký thành công!", Toast.Type.SUCCESS, 2, this::navigateToLogin);
    }
    // ── Helper private ───────────────────────────────────────────────────────
    //Phương thức tạo RegisterRequest (không cần handleRegister phải biết -> SRP)
    private RegisterRequest buildRequest() {
        return new RegisterRequest(
                fullNameField.getText().trim(),
                usernameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                dobField.getValue() == null ? null : dobField.getValue().toString(),
                passwordField.getText(),
                confirmPasswordField.getText()
        );
    }

    // FIX 1: Commit giá trị đang gõ dở trong DatePicker
    private void commitDatePickerValue() {
        String text = dobField.getEditor().getText();
        if (text != null && !text.isBlank() && dobField.getValue() == null) {
            LocalDate parsed = dobField.getConverter().fromString(text);
            dobField.setValue(parsed); // có thể null nếu sai format — Validator sẽ bắt
        }
    }

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