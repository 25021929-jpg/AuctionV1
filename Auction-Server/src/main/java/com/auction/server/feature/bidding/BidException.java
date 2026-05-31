package com.auction.server.feature.bidding;

/**
 * Exception đại diện cho lỗi nghiệp vụ trong luồng đặt giá.
 *
 * Phân loại exception trong hệ thống:
 *
 *   BidException (extends RuntimeException):
 *     → Lỗi do nghiệp vụ: giá không hợp lệ, phiên đã kết thúc...
 *     → Controller catch → trả HTTP 400 + message cho user
 *     → Không cần log stack trace (lỗi do user, không phải hệ thống)
 *
 *   RuntimeException thông thường:
 *     → Lỗi hệ thống: DB down, network timeout...
 *     → DbExecutor tự rollback
 *     → Controller catch → trả HTTP 500
 *     → Cần log stack trace để debug
 *
 * Tại sao extends RuntimeException thay vì Exception?
 *   Checked Exception (extends Exception) buộc caller phải try-catch.
 *   Unchecked Exception (extends RuntimeException) không bắt buộc.
 *   Business exception lan qua nhiều tầng → Unchecked tiện hơn.
 *   DbExecutor, Service, Controller đều có thể bắt khi cần.
 */
public class BidException extends RuntimeException {

    public BidException(String message) {
        super(message);
    }

    public BidException(String message, Throwable cause) {
        super(message, cause);
    }
}