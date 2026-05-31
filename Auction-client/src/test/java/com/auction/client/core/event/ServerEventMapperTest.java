package com.auction.client.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.auction.shared.protocol.ActionConstants;
import org.junit.jupiter.api.Test;

class ServerEventMapperTest {

  @Test
  void map_null_returnsNetworkEvent() {
    assertEquals(EventType.NETWORK_EVENT, ServerEventMapper.map(null));
  }

  @Test
  void map_knownBidUpdated_returnsBidUpdated() {
    assertEquals(EventType.BID_UPDATED, ServerEventMapper.map(ActionConstants.EVENT_BID_UPDATED));
  }

  @Test
  void map_knownStatusChanged_returnsStatusChanged() {
    assertEquals(
        EventType.AUCTION_STATUS_CHANGED,
        ServerEventMapper.map(ActionConstants.EVENT_AUCTION_STATUS_CHANGED));
  }

  @Test
  void map_unknown_returnsNetworkEvent() {
    assertEquals(EventType.NETWORK_EVENT, ServerEventMapper.map("SOME_UNKNOWN_EVENT"));
  }
}
