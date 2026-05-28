package com.auction.client.core.event;

public enum EventType {
    /** Server push event (chưa map sang loại cụ thể). */
    NETWORK_EVENT,

    BID_UPDATED,
    AUCTION_STATUS_CHANGED,

    /** Kết nối socket bị lỗi/đứt. */
    CONNECTION_LOST
}
