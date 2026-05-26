package com.auction.client.core.event;

import com.auction.shared.protocol.ActionConstants;

/**
 * Map action server-push -> EventType trong client.
 *
 * <p>Để hạn chế sửa code rải rác, chỉ map tên action tại đây (và ActionConstants).
 */
public final class ServerEventMapper {

    private ServerEventMapper() {}

    public static EventType map(String action) {
        if (action == null) return EventType.NETWORK_EVENT;

        if (ActionConstants.EVENT_BID_UPDATED.equals(action)) {
            return EventType.BID_UPDATED;
        }
        if (ActionConstants.EVENT_AUCTION_STATUS_CHANGED.equals(action)) {
            return EventType.AUCTION_STATUS_CHANGED;
        }

        return EventType.NETWORK_EVENT;
    }
}
