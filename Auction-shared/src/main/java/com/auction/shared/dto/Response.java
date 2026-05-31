package com.auction.shared.dto;

/**
 * Generic response payload used inside WireMessage.data-aware client handling.
 *
 * <p>In the socket protocol, success/message/data may also be carried directly on WireMessage. This
 * class is kept as the typed response abstraction used by client services and server controllers.
 * The optional errorCode field helps client classify business errors without parsing message text.
 */
public class Response<T> {

  private boolean success;
  private String message;
  private T data;
  private String errorCode;

  /** No-arg constructor required by Gson/serialization. */
  public Response() {}

  public Response(boolean success, String message, T data) {
    this(success, message, data, null);
  }

  public Response(boolean success, String message, T data, String errorCode) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.errorCode = errorCode;
  }

  public static <T> Response<T> success(String message, T data) {
    return new Response<>(true, message, data);
  }

  public static <T> Response<T> fail(String message) {
    return new Response<>(false, message, null);
  }

  public static <T> Response<T> fail(String message, String errorCode) {
    return new Response<>(false, message, null, errorCode);
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setData(T data) {
    this.data = data;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
}
