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

        if (endTime == null) {
            errors.add(new FieldError("endTime", "Thời gian kết thúc không được rỗng"));
        }

        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            errors.add(new FieldError("endTime", "Thời gian bắt đầu phải trước thời gian kết thúc"));
        }

        return ValidationResult.from(errors);
    }
}
