package com.auction.server.feature.auction;

/**
 * Custom exception cho toàn bộ feature auction.
 * Ném ra khi có lỗi nghiệp vụ — Controller sẽ bắt và trả Response.fail().
 */

public class AuctionException extends RuntimeException {

    public AuctionException(String message) {
        super(message);
    }

    public AuctionException(String message, Throwable cause) {
        super(message, cause);
    }
}