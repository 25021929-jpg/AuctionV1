package com.auction.client.feature.auth.controller;

import com.auction.client.core.ui.SceneNavigator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ForgotPasswordController {

    // Bước 1
    @FXML private VBox stepEmail;
    @FXML private TextField emailField;
    @FXML private Label emailError;

    // Bước 2
    @FXML private VBox stepOtp;
    @FXML private TextField otpField;
    @FXML private Label otpError, otpHint, countdownLabel;
    @FXML private Button resendBtn;

    // Bước 3
    @FXML private VBox stepReset;
    @FXML private PasswordField newPasswordField, confirmPasswordField;
    @FXML private Label resetError, strengthLabel;
    @FXML private ProgressBar strengthBar;

    // Bước 4
    @FXML private VBox stepSuccess;

    private String verifiedEmail = "";
    private Timeline countdown;
    private int seconds = 60;

    @FXML
    public void initialize() {
        newPasswordField.textProperty().addListener((o, old, val) -> updateStrength(val));
    }

    // ── Bước 1: Gửi OTP ──
    @FXML
    private void handleSendOtp() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) { showError(emailError, "Vui lòng nhập email."); return; }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError(emailError, "Email không đúng định dạng."); return;
        }
        hideError(emailError);
        verifiedEmail = email;
        otpHint.setText("Mã đã gửi đến: " + email);
        // TODO: authService.sendOtp(email)
        goTo(stepOtp);
        startCountdown();
    }

    // ── Bước 2: Xác nhận OTP ──
    @FXML
    private void handleVerifyOtp() {
        String otp = otpField.getText().trim();
        if (otp.length() != 6 || !otp.matches("\\d+")) {
            showError(otpError, "Mã OTP phải gồm đúng 6 chữ số."); return;
        }
        hideError(otpError);
        // TODO: authService.verifyOtp(verifiedEmail, otp)
        stopCountdown();
        goTo(stepReset);
    }

    @FXML private void handleResendOtp() { otpField.clear(); hideError(otpError); startCountdown(); }
    @FXML private void handleBackToEmail() { stopCountdown(); otpField.clear(); goTo(stepEmail); }

    // ── Bước 3: Đặt lại mật khẩu ──
    @FXML
    private void handleResetPassword() {
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();
        if (pass.length() < 8) { showError(resetError, "Mật khẩu phải có ít nhất 8 ký tự."); return; }
        if (!pass.equals(confirm)) { showError(resetError, "Mật khẩu xác nhận không khớp."); return; }
        hideError(resetError);
        // TODO: authService.resetPassword(verifiedEmail, pass)
        goTo(stepSuccess);
    }

    @FXML
    private void handleNavigateLogin() {
        SceneNavigator.switchScene("/com/auction/client/view/login-view.fxml");
    }

    // ── Helper: chuyển bước ──
    private void goTo(VBox target) {
        for (VBox step : new VBox[]{stepEmail, stepOtp, stepReset, stepSuccess}) {
            step.setVisible(false); step.setManaged(false);
        }
        target.setVisible(true); target.setManaged(true);
    }

    // ── Helper: đếm ngược ──
    private void startCountdown() {
        seconds = 60;
        resendBtn.setVisible(false); resendBtn.setManaged(false);
        countdownLabel.setVisible(true); countdownLabel.setManaged(true);
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdownLabel.setText(--seconds + "s");
            if (seconds <= 0) {
                stopCountdown();
                countdownLabel.setVisible(false); countdownLabel.setManaged(false);
                resendBtn.setVisible(true); resendBtn.setManaged(true);
            }
        }));
        countdown.setCycleCount(60);
        countdown.play();
    }

    private void stopCountdown() { if (countdown != null) countdown.stop(); }

    // ── Helper: độ mạnh mật khẩu ──
    private void updateStrength(String p) {
        int s = 0;
        if (p.length() >= 8)                        s++;
        if (p.length() >= 12)                       s++;
        if (p.matches(".*[A-Z].*"))                 s++;
        if (p.matches(".*[0-9].*"))                 s++;
        if (p.matches(".*[!@#$%^&*()_+\\-=].*"))   s++;

        if (s <= 1) {
            strengthBar.setProgress(0.2); strengthLabel.setText("Yếu");
            strengthLabel.setStyle("-fx-text-fill:#FF3D5A;-fx-font-weight:bold;-fx-font-size:12px;");
            strengthBar.setStyle("-fx-accent:#FF3D5A;");
        } else if (s <= 3) {
            strengthBar.setProgress(0.6); strengthLabel.setText("Trung bình");
            strengthLabel.setStyle("-fx-text-fill:#C8A84B;-fx-font-weight:bold;-fx-font-size:12px;");
            strengthBar.setStyle("-fx-accent:#C8A84B;");
        } else {
            strengthBar.setProgress(1.0); strengthLabel.setText("Mạnh");
            strengthLabel.setStyle("-fx-text-fill:#00E5A0;-fx-font-weight:bold;-fx-font-size:12px;");
            strengthBar.setStyle("-fx-accent:#00E5A0;");
        }
    }

    // ── Helper: lỗi ──
    private void showError(Label l, String msg) { l.setText(msg); l.setVisible(true); l.setManaged(true); }
    private void hideError(Label l) { l.setVisible(false); l.setManaged(false); }

    public void handleNavigateLogin(ActionEvent actionEvent) {
        SceneNavigator.switchScene("/com/auction/client/feature/auth/view/login-view.fxml");
    }
}
