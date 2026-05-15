package com.auction.shared.dto;

public class Response<T> {

    private boolean success;
    private String message;
    private T data;

    // No-arg constructor: cần cho Gson/serialization an toàn
    public Response() {}

    public Response(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Thành công có message + data
    public static <T> Response<T> success(String message, T data) {
        return new Response<>(true, message, data);
    }

    // Thất bại chỉ cần message
    public static <T> Response<T> fail(String message) {
        return new Response<>(false, message, null);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    // Setters (cho phép tạo object rỗng rồi set fields)
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }
}