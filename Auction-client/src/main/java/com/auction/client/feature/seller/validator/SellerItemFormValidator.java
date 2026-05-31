package com.auction.client.feature.seller.validator;

import com.auction.client.core.util.MoneyFormat;
import com.auction.validation.FieldError;
import com.auction.validation.ValidationResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Validate form thêm/sửa sản phẩm của Seller trước khi gửi request. */
public final class SellerItemFormValidator {

    private static final int MAX_NAME_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    private SellerItemFormValidator() {
    }

    public static ValidationResult validate(String name,
                                            String description,
                                            String startPriceText,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime) {
        return validate(name, description, startPriceText, startTime, endTime, false);
    }

    public static ValidationResult validate(String name,
                                            String description,
                                            String startPriceText,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime,
                                            boolean requireStartNotPast) {
        List<FieldError> errors = new ArrayList<>();

        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            errors.add(new FieldError("name", "Tên sản phẩm không được rỗng"));
        } else if (normalizedName.length() > MAX_NAME_LENGTH) {
            errors.add(new FieldError("name", "Tên sản phẩm tối đa " + MAX_NAME_LENGTH + " ký tự"));
        }

        String normalizedDescription = description == null ? "" : description.trim();
        if (normalizedDescription.isEmpty()) {
            errors.add(new FieldError("description", "Mô tả sản phẩm không được rỗng"));
        } else if (normalizedDescription.length() > MAX_DESCRIPTION_LENGTH) {
            errors.add(new FieldError("description", "Mô tả tối đa " + MAX_DESCRIPTION_LENGTH + " ký tự"));
        }

        try {
            BigDecimal startPrice = MoneyFormat.parse(startPriceText);
            if (startPrice.signum() <= 0) {
                errors.add(new FieldError("startPrice", "Giá khởi điểm phải lớn hơn 0"));
            }
        } catch (Exception ex) {
            errors.add(new FieldError("startPrice", "Giá khởi điểm phải là số hợp lệ"));
        }

        if (startTime == null) {
            errors.add(new FieldError("startTime", "Thời gian bắt đầu không được rỗng"));
        }

        if (endTime == null) {
            errors.add(new FieldError("endTime", "Thời gian kết thúc không được rỗng"));
        }

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        /*
         * Cho phép startTime bằng hoặc nhỏ hơn thời điểm hiện tại để Seller có thể
         * tạo phiên mở ngay. Điều kiện quan trọng là endTime phải còn ở tương lai
         * và startTime phải đứng trước endTime. Việc này cũng tránh lỗi race-condition
         * khi người dùng nhập đúng phút hiện tại nhưng bấm OK sau vài giây.
         */
        if (endTime != null && !endTime.isAfter(now)) {
            errors.add(new FieldError("endTime", "Thời gian kết thúc phải lớn hơn thời gian hiện tại"));
        }
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            errors.add(new FieldError("endTime", "Thời gian bắt đầu phải trước thời gian kết thúc"));
        }

        return ValidationResult.from(errors);
    }
}
