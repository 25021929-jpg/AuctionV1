package com.auction.client.core.event;

import com.google.gson.JsonElement;

/**
 * Payload chuẩn cho các server-push event khi client chưa map sang event domain cụ thể.
 *
 * <p>action: tên event (ví dụ: EVENT_BID_UPDATED) data: payload JSON đi kèm
 */
public class NetworkEventPayload {

  private final String action;
  private final JsonElement data;

  public NetworkEventPayload(String action, JsonElement data) {
    this.action = action;
    this.data = data;
  }

  public String getAction() {
    return action;
  }

  public JsonElement getData() {
    return data;
  }
}
