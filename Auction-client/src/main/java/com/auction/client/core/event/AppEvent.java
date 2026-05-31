package com.auction.client.core.event;

/**
 * Event tổng quát cho client. payload: kiểu Object để linh hoạt, nhưng mỗi listener phải tự cast
 * đúng kiểu.
 */
public record AppEvent(EventType type, Object payload) {}
