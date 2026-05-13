package com.auction.shared.validation;
//Record là một class đặc biệt chỉ để lưu trữ giữ liệu (và nó sẽ là immutable
//và nó đã được tích hợp sẵn những hàm getter giành cho field - tênclass.tênField()
public record FieldError(String field, String message) {
}
