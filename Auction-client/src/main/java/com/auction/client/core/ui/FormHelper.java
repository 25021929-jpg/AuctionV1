package com.auction.client.core.ui;

import com.auction.validation.ValidationResult;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * Utility class xử lý hiển thị/xóa lỗi trên các form field JavaFX.
 *
 * Cách dùng trong Controller:
 *
 *   // 1. Khai báo map (giữ thứ tự bằng LinkedHashMap) để tránh việc phải lặp code
 *   private final Map<Control, Label> fieldErrorMap = new LinkedHashMap<>();
 *
 *   // 2. Gắn trong initialize()
 *   FormHelper.register(fieldErrorMap,
 *       emailField,    emailError,
 *       passwordField, passwordError
 *   );
 *   FormHelper.bindClearOnChange(fieldErrorMap);
 *
 *   // 3. Khi submit
 *   FormHelper.clearAll(fieldErrorMap);
 *   ValidationResult result = validator.validate(request);
 *   if (!result.valid()) {
 *       Map<String, Control> fieldMap = Map.of("email", emailField, ...);
 *       FormHelper.applyErrors(result, fieldMap, fieldErrorMap);
 *   }
 */
public class FormHelper {
    /**
     * Hiển thị lỗi cho một field thường (TextField, PasswordField, ...).
     */
    public static void showError(Control field, Label errorLabel, String message) {
        field.getStyleClass().add("field-error"); // ← dùng CSS class thay hardcode
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    /*
     * Hiển thị lỗi cho DatePicker — target đúng inner editor.
     */
    public static void showDatePickerError(DatePicker picker, Label errorLabel, String message) {
        picker.getEditor().getStyleClass().add("field-error"); // ← TextField bên trong
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    // ─── Áp dụng ValidationResult vào form ───────────────────────────────────

    /**
     * Áp dụng tất cả lỗi từ ValidationResult vào các field tương ứng.
     *
     /*
     Hàm applyError nhận (ValidationResult, Map field: Control của các Field,Map control của các Field : Label FieldError và kiểm tra nếu có lỗi thì show lỗi
     -> Có thể tái sử dụng, không bị lặp if (cách tốt khi refractor code, dễ mở rộng,...-> thỏa mãn các nguyên lý)
     */
    public static void applyErrors(
            ValidationResult result,
            Map<String, Control> fieldMap,
            Map<Control, Label> errorMap) {

        fieldMap.forEach((fieldName, control) -> {
            if (result.hasErrorFor(fieldName)) {
                Label errorLabel = errorMap.get(control);
                if (errorLabel == null) return; //Nếu không có errorLabel tương ứng

                String message = result.errorFor(fieldName);
                if (control instanceof DatePicker dp) {
                    showDatePickerError(dp, errorLabel, message);
                } else {
                    showError(control, errorLabel, message);
                }
            }
        });
    }
    /*
    Lưu ý: datePicker không giống các Node khác , nó là một node được tạo thành từ 2 node
    DatePicker (node ngoài)
    │
    ├─ TextField (getEditor())   ← phần ô nhập text
    └─ Button                    ← phần icon lịch
    Nếu bạn set style cho DatePicker ngoài:
    DatePicker [                    📅]  ← border đỏ bao cả button lịch
                                         trông rất xấu

    Nếu bạn set style cho Editor bên trong:
    DatePicker [____________________📅]  ← border đỏ chỉ bao ô text
                                         đúng như mong muốn
     Tóm lại:  getEditor() sẽ trả về node TextField của datepicker nên cũng add(css) như các node textField khác
     */

    /**
    Lưu ý: Các tham số Control Field của các hàm trong class này sẽ đều là Control để có thể xử lý trường hợp DatePicker
    Nó sẽ kiểm tra có phải là datePicker hay không, Nếu có thì DownCast nó thành DatePicker và gọi hàm lấy node Text của nó.
     */
    public static void clearError(Control field, Label errorLabel) {
        field.getStyleClass().remove("field-error");
        if (field instanceof DatePicker dp) {
            dp.getEditor().getStyleClass().remove("field-error");
        }
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Xóa lỗi cho tất cả field trong map.
     * forEach của Map nhận BiConsumer<K, V> nên viết được cả 2 cách:
     * // Method reference
     * fieldErrorMap.forEach(FormHelper::clearError);
     * // Lambda tương đương
     * fieldErrorMap.forEach((field, label) -> FormHelper.clearError(field, label));
     * BiConsumer<T, U> yêu cầu một function nhận 2 tham số, không trả về gì:
     */
    public static void clearAll(Map<Control, Label> fieldErrorMap) {
        fieldErrorMap.forEach((field, errorLabel)-> FormHelper.clearError(field, errorLabel) );
    }


    // ─── Bind listener tự động xóa lỗi khi user chỉnh sửa ───────────────────

    /**
     * Gắn listener vào tất cả field: khi user gõ/chọn → tự clear error.
     * Gọi một lần trong initialize() sau khi register() xong.
     */
    public static void bindClearOnChange(Map<Control, Label> fieldErrorMap) {
        fieldErrorMap.forEach((field, errorLabel) -> {
            if (field instanceof DatePicker dp) {
                dp.valueProperty().addListener(
                        (o, old, val) -> clearError(field, errorLabel)
                );
            }
            else {
                // Với TextField/PasswordField: clear ngay khi gõ
                tryBindTextChange(field, errorLabel); // ← TextField/PasswordField
            }
        });
    }

    /**
     * Gắn textProperty listener nếu field có text (TextField, PasswordField).
     * Dùng pattern matching để tránh import javafx.scene.control.TextInputControl.
     */
    public static void tryBindTextChange(Control field, Label errorLabel) {
        if (field instanceof javafx.scene.control.TextInputControl tic) {
            tic.textProperty().addListener(
                    (o, old, val) -> clearError(field, errorLabel)
            );
        }
    }


}