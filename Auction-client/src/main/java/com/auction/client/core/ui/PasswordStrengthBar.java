package com.auction.client.core.ui;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
public class PasswordStrengthBar {

    private final ProgressBar bar;
    private final Label label;

    // Inject ProgressBar và Label từ Controller vào
    public PasswordStrengthBar(ProgressBar bar, Label label) {
        this.bar   = bar;
        this.label = label;
    }
    // Gắn listener vào PasswordField — tự động update khi gõ
    public void bindTo(PasswordField field) {
        field.textProperty().addListener(
                (obs, oldVal, newVal) -> update(newVal)
        );
    }
    // Update dựa trên password hiện tại
    public void update(String password) {
        int score = calcScore(password);
        applyStyle(score);
    }
    // ── Tính điểm ────────────────────────────────────────
    private int calcScore(String p) {
        if (p == null || p.isBlank()) return 0;
        int score = 0;
        // 1. Kiểm tra độ dài
        if (p.length() >= 8)  score++;
        if (p.length() >= 12) score++;

        // 2. Kiểm tra các loại ký tự (Sử dụng Regex)
        if (p.matches(".*[A-Z].*")) score++; // Ít nhất 1 chữ hoa
        if (p.matches(".*[a-z].*")) score++; // Ít nhất 1 chữ thường (MỚI)
        if (p.matches(".*[0-9].*")) score++; // Ít nhất 1 chữ số
        if (p.matches(".*[!@#$%^&*()_+\\-=].*")) score++; // Ít nhất 1 ký tự đặc biệt

        return score;
    }
    /*
    Giaỉ thích hàm tính điểm mật khẩu:
    -nếu thỏa mãn 1 tiêu chí thì ộng 1 điểm
    VD:
    .*:Dấu chấm . đại diện cho bất kỳ ký tự nào (ngoại trừ dòng mới).
    Dấu sao * có nghĩa là "lặp lại 0 hoặc nhiều lần".
    Kết hợp lại, .* có nghĩa là khớp với một chuỗi ký tự bất kỳ, có độ dài bất kỳ (hoặc không có ký tự nào).
    [A-Z]:Đây là một "Character Set" (tập hợp ký tự) dùng để khớp với một chữ cái viết hoa duy nhất từ 'A' đến 'Z'.
    .*:Tương tự như phần đầu, phần này cho phép bất kỳ ký tự nào xuất hiện sau chữ cái viết hoa đó.
    Tóm tắt ý nghĩaKhi ghép lại, toàn bộ biểu thức .*[A-Z].* sẽ khớp với bất kỳ dòng hoặc chuỗi văn bản nào, miễn là trong đó có sự xuất hiện của ít nhất một ký tự in hoa.
     */


    // ── Áp style theo điểm ───────────────────────────────
    private void applyStyle(int score) {
        if (score <= 1) {
            set("Yếu",   0.2,
                    "#FF3D5A");
        } else if (score <= 4) {
            set("Trung bình",   0.6,
                    "#C8A84B");
        } else {
            set("Mạnh", 1.0,
                    "#00E5A0");
        }
    }
    private void set(String text, double progress, String color) {
        label.setText(text);
        label.setStyle(
                "-fx-text-fill:" + color + ";" +
                        "-fx-font-weight:bold;" +
                        "-fx-font-size:12px;"
        );
        bar.setProgress(progress);
        bar.setStyle("-fx-accent:" + color + ";");
    }
}