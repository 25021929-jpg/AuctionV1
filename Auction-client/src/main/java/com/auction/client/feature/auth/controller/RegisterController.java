package com.auction.client.feature.auth.controller;

import com.auction.client.core.error.ErrorHandler;
import com.auction.client.core.ui.FormHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.client.core.ui.Toast;
import com.auction.client.core.ui.UIAnimations;
import com.auction.client.feature.auth.factory.AuthValidatorFactory;
import com.auction.client.feature.auth.service.AuthService;
import com.auction.client.feature.auth.service.AuthServiceImpl;
import com.auction.shared.dto.auth.request.RegisterRequest;
import com.auction.validation.ValidationResult;
import com.auction.validation.Validator;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class RegisterController {

  @FXML private TextField fullNameField, usernameField;
  @FXML private TextField emailField, phoneField;
  @FXML private DatePicker dobField;
  @FXML private RadioButton bidderRoleRadio;
  @FXML private RadioButton sellerRoleRadio;
  @FXML private PasswordField passwordField, confirmPasswordField;
  @FXML private Button registerBtn;
  @FXML private VBox formBox;

  @FXML private Label fullNameError, usernameError;
  @FXML private Label emailError, phoneError;
  @FXML private Label passwordError, confirmPasswordError;
  @FXML private Label dobError;

  private final Map<Control, Label> fieldErrorMap = new LinkedHashMap<>();
  private final Map<String, Control> fieldMap = new LinkedHashMap<>();

  private final AuthService authService;
  private final Validator<RegisterRequest> validator;

  public RegisterController() {
    this(new AuthServiceImpl(), AuthValidatorFactory.createRegisterValidator());
  }

  public RegisterController(AuthService authService, Validator<RegisterRequest> validator) {
    this.authService = authService;
    this.validator = validator;
  }

  @FXML
  public void initialize() {
    UIAnimations.entrance(formBox);
    setupDatePicker();
    setupRoleButtons();
    setupFormHelper();
  }

  private void setupDatePicker() {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy");
    dobField.setConverter(
        new StringConverter<>() {
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
  }

  private void setupRoleButtons() {
    if (bidderRoleRadio == null || sellerRoleRadio == null) {
      return;
    }
    ToggleGroup group = new ToggleGroup();
    bidderRoleRadio.setToggleGroup(group);
    sellerRoleRadio.setToggleGroup(group);
    bidderRoleRadio.setSelected(true);
  }

  private void setupFormHelper() {
    fieldErrorMap.put(usernameField, usernameError);
    fieldErrorMap.put(fullNameField, fullNameError);
    fieldErrorMap.put(emailField, emailError);
    fieldErrorMap.put(phoneField, phoneError);
    fieldErrorMap.put(passwordField, passwordError);
    fieldErrorMap.put(confirmPasswordField, confirmPasswordError);
    fieldErrorMap.put(dobField, dobError);

    fieldMap.put("fullName", fullNameField);
    fieldMap.put("username", usernameField);
    fieldMap.put("email", emailField);
    fieldMap.put("phoneNumber", phoneField);
    fieldMap.put("password", passwordField);
    fieldMap.put("confirmPassword", confirmPasswordField);
    fieldMap.put("birthDate", dobField);

    FormHelper.bindClearOnChange(fieldErrorMap);
  }

  @FXML
  private void handleRegister(ActionEvent event) {
    FormHelper.clearAll(fieldErrorMap);
    commitDatePickerValue();

    RegisterRequest request = buildRequest();
    ValidationResult result = validator.validate(request);
    if (!result.valid()) {
      FormHelper.applyErrors(result, fieldMap, fieldErrorMap);
      return;
    }

    registerBtn.setDisable(true);

    Thread thread =
        new Thread(
            () -> {
              try {
                authService.register(request);
                Platform.runLater(
                    () -> {
                      registerBtn.setDisable(false);
                      showToast(
                          "✓ Đăng ký thành công!", Toast.Type.SUCCESS, 2, this::navigateToLogin);
                    });
              } catch (IOException e) {
                Platform.runLater(
                    () -> {
                      registerBtn.setDisable(false);
                      showToast(ErrorHandler.getUserMessage(e), Toast.Type.ERROR, 3, null);
                    });
              }
            },
            "auth-register-thread");

    thread.setDaemon(true);
    thread.start();
  }

  private RegisterRequest buildRequest() {
    return new RegisterRequest(
        valueOf(fullNameField),
        valueOf(usernameField),
        valueOf(emailField),
        normalizedPhone(),
        passwordField.getText(),
        confirmPasswordField.getText(),
        dobField.getValue(),
        selectedRole());
  }

  private String selectedRole() {
    if (sellerRoleRadio != null && sellerRoleRadio.isSelected()) {
      return "SELLER";
    }
    return "BIDDER";
  }

  private String valueOf(TextField field) {
    String value = field.getText();
    return value == null ? "" : value.trim();
  }

  private String normalizedPhone() {
    return valueOf(phoneField).replaceAll("[\\s.-]", "");
  }

  private void commitDatePickerValue() {
    String text = dobField.getEditor().getText();
    if (text != null && !text.isBlank() && dobField.getValue() == null) {
      LocalDate parsed = dobField.getConverter().fromString(text);
      dobField.setValue(parsed);
    }
  }

  private void navigateToLogin() {
    SceneNavigator.switchScene(ScenePaths.LOGIN);
  }

  @FXML
  private void handleNavigateLogin(ActionEvent actionEvent) {
    navigateToLogin();
  }

  private void showToast(String message, Toast.Type type, int seconds, Runnable onFinished) {
    StackPane root = (StackPane) registerBtn.getScene().getRoot();
    Toast.show(root, message, type, seconds, onFinished);
  }
}
