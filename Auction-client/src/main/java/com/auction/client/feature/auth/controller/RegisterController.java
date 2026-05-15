package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.FormHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.Toast;
import com.auction.client.core.ui.UIAnimations;
import com.auction.client.feature.auth.dto.request.RegisterPayload;
import com.auction.client.feature.auth.dto.request.RegisterRequest;
import com.auction.client.feature.auth.factory.AuthValidatorFactory;
import com.auction.client.network.ServerCommunicator;
import com.auction.client.network.SocketClient;
import com.auction.shared.dto.Response;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegisterController {

    // ── FXML ─────────────────────────────────────────────────────
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

    // ── Form helpers ──────────────────────────────────────────────
    private final Map<Control, Label>  fieldErrorMap = new LinkedHashMap<>();
    private final Map<String, Control> fieldMap      = new LinkedHashMap<>();

    // ── Dependencies ──────────────────────────────────────────────
    private final ServerCommunicator         communicator;
    private final Validator<RegisterRequest> validator;

    // Constructor mặc định — JavaFX FXML dùng cái này
    public RegisterController() {
        this(
                SocketClient.getInstance(),
                AuthValidatorFactory.createRegisterValidator()
        );
    }

    // Constructor cho test — inject từ ngoài vào
    public RegisterController(
            ServerCommunicator communicator,
            Validator<RegisterRequest> validator) {
        this.communicator = communicator;
        this.validator    = validator;
    }

    // ── initialize ────────────────────────────────────────────────
    @FXML
    public void initialize() {
        UIAnimations.entrance(formBox);
        setupDatePicker();
        setupFormHelper();
    }

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
        fieldErrorMap.put(usernameField,        usernameError);
        fieldErrorMap.put(fullNameField,         fullNameError);
        fieldErrorMap.put(emailField,            emailError);
        fieldErrorMap.put(phoneField,            phoneError);
        fieldErrorMap.put(passwordField,         passwordError);
        fieldErrorMap.put(confirmPasswordField,  confirmPasswordError);
        fieldErrorMap.put(dobField,              dobError);

        fieldMap.put("fullName",        fullNameField);
        fieldMap.put("username",        usernameField);
        fieldMap.put("email",           emailField);
        fieldMap.put("phoneNumber",     phoneField);
        fieldMap.put("password",        passwordField);
        fieldMap.put("confirmPassword", confirmPasswordField);
        fieldMap.put("birthDate",       dobField);

        FormHelper.bindClearOnChange(fieldErrorMap);
    }

    // ── handleRegister ────────────────────────────────────────────
    @FXML
    private void handleRegister(ActionEvent event) {

        // 1. Clear lỗi cũ
        FormHelper.clearAll(fieldErrorMap);

        // 2. Commit DatePicker nếu đang gõ dở
        commitDatePickerValue();

        // 3. Build + Validate
        RegisterRequest request = buildRequest();
        ValidationResult result = validator.validate(request);
        if (!result.valid()) {
            FormHelper.applyErrors(result, fieldMap, fieldErrorMap);
            return;
        }
        RegisterPayload requestForServer = new RegisterPayload(request);

        // 4. Kiểm tra đã kết nối server chưa
        if (!SocketClient.getInstance().isConnected()) {
            StackPane root = (StackPane) registerBtn.getScene().getRoot();
            Toast.show(root,
                    "Đang kết nối server, vui lòng thử lại!",
                    Toast.Type.WARNING, 2, null);
            return;
        }

        // 5. Disable button tránh bấm nhiều lần
        registerBtn.setDisable(true);

        // 6. Gửi request trên background thread
        Thread thread = new Thread(() -> {
            try {
                Response<Void> response =
                        communicator.send("AUTH_REGISTER", requestForServer, Void.class);

                Platform.runLater(() -> {
                    registerBtn.setDisable(false);

                    StackPane root = (StackPane) registerBtn.getScene().getRoot();

                    if (response.isSuccess()) {
                        Toast.show(root, "✓ Đăng ký thành công!",
                                Toast.Type.SUCCESS, 2, this::navigateToLogin);
                    } else {
                        // Server trả về lỗi nghiệp vụ
                        // (username đã tồn tại, email đã dùng...)
                        Toast.show(root, response.getMessage(),
                                Toast.Type.ERROR, 3, null);
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() -> {
                    registerBtn.setDisable(false);
                    StackPane root = (StackPane) registerBtn.getScene().getRoot();
                    Toast.show(root, "Lỗi kết nối, vui lòng thử lại!",
                            Toast.Type.ERROR, 3, null);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    // ── Helper private ────────────────────────────────────────────
    private RegisterRequest buildRequest() {
        return new RegisterRequest(
                fullNameField.getText(),
                usernameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                dobField.getValue()
        );
    }

    private void commitDatePickerValue() {
        String text = dobField.getEditor().getText();
        if (text != null && !text.isBlank() && dobField.getValue() == null) {
            LocalDate parsed = dobField.getConverter().fromString(text);
            dobField.setValue(parsed);
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