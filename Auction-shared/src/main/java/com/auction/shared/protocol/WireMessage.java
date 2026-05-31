package com.auction.shared.protocol;

import com.google.gson.JsonElement;

/**
 * Envelope chuẩn cho giao tiếp Socket (1 line JSON / 1 message).
 *
 * <p>Mục tiêu:
 * <ul>
 *   <li>Client có thể vừa nhận RESPONSE, vừa nhận EVENT realtime mà không bị tranh chấp luồng đọc.</li>
 *   <li>Server/Client dùng chung class để "khớp" protocol.</li>
 * </ul>
 */
public class WireMessage {

    /** REQUEST / RESPONSE / EVENT */
    private WireMessageType type;

    /** Dùng để match RESPONSE cho REQUEST (bắt buộc với RESPONSE). */
    private String requestId;

    /** Tên action/command. */
    private String action;

    /** Chỉ dùng cho RESPONSE. */
    private boolean success;

    /** Chỉ dùng cho RESPONSE (hoặc error event nếu muốn). */
    private String message;

    /** Mã lỗi máy đọc được, optional. */
    private String errorCode;

    /** Payload JSON (request body / response data / event data). */
    private JsonElement data;

    public WireMessageType getType() {
        return type;
    }

    public void setType(WireMessageType type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }
}
