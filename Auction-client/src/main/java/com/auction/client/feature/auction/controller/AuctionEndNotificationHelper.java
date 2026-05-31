package com.auction.client.feature.auction.controller;

import com.auction.client.core.event.AppEvent;
import com.auction.client.core.event.NetworkEventPayload;
import com.auction.client.core.session.UserSession;
import com.auction.client.core.ui.AlertHelper;
import com.auction.shared.domain.AuctionStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hiển thị thông báo khi phiên đấu giá kết thúc.
 *
 * <p>Helper này được dùng lại ở nhiều màn hình để tránh trùng logic và tránh hiện nhiều popup cho
 * cùng một người dùng/cùng một phiên.
 */
public final class AuctionEndNotificationHelper {

  private static final Set<String> SHOWN_KEYS = ConcurrentHashMap.newKeySet();

  private AuctionEndNotificationHelper() {}

  /**
   * Xử lý EVENT_AUCTION_STATUS_CHANGED nếu trạng thái mới là ENDED.
   *
   * @param event event nhận từ socket
   * @param currentAuctionId auction đang xem; có thể null nếu màn hình là danh sách
   * @param showGenericForCurrentAuction true nếu muốn báo chung cho người đang xem phiên nhưng chưa
   *     đặt giá
   */
  public static void showIfEnded(
      AppEvent event, Long currentAuctionId, boolean showGenericForCurrentAuction) {
    if (!(event.payload() instanceof NetworkEventPayload payload)) {
      return;
    }
    JsonElement dataEl = payload.getData();
    if (dataEl == null || !dataEl.isJsonObject()) {
      return;
    }

    JsonObject data = dataEl.getAsJsonObject();
    Long auctionId = getLong(data, "auctionId");
    String statusText = getString(data, "status");
    if (auctionId == null || statusText == null) {
      return;
    }
    AuctionStatus status = AuctionStatus.fromString(statusText);
    if (status != AuctionStatus.ENDED) {
      return;
    }

    Long currentUserId = UserSession.getInstance().getUserId();
    if (currentUserId == null || currentUserId <= 0) {
      return;
    }

    Long sellerId = getLong(data, "sellerId");
    Long winnerId = getLong(data, "winnerId");
    String itemName = safeText(getString(data, "itemName"), "sản phẩm đấu giá");
    String winnerUsername = safeText(getString(data, "winnerUsername"), "người thắng");
    boolean participated = containsUserId(data, "participantIds", currentUserId);
    boolean isCurrentAuction = currentAuctionId != null && currentAuctionId.equals(auctionId);

    String title = null;
    String message = null;

    if (currentUserId.equals(sellerId)) {
      title = "Phiên đấu giá đã kết thúc";
      if (winnerId != null && winnerId > 0) {
        message =
            "Đã bán thành công "
                + itemName
                + " (phiên #"
                + auctionId
                + "). Người thắng: "
                + winnerUsername
                + ".";
      } else {
        message =
            "Phiên #"
                + auctionId
                + " của "
                + itemName
                + " đã kết thúc nhưng chưa có người mua thành công.";
      }
    } else if (winnerId != null && currentUserId.equals(winnerId)) {
      title = "Chúc mừng!";
      message = "Bạn đã đấu giá thành công " + itemName + " (phiên #" + auctionId + ").";
    } else if (participated) {
      title = "Phiên đấu giá đã kết thúc";
      message =
          "Phiên #"
              + auctionId
              + " của "
              + itemName
              + " đã kết thúc. Bạn không phải người thắng cuộc.";
    } else if (showGenericForCurrentAuction && isCurrentAuction) {
      title = "Phiên đấu giá đã kết thúc";
      message = "Phiên #" + auctionId + " của " + itemName + " đã kết thúc.";
    }

    if (title == null || message == null) {
      return;
    }

    String key = currentUserId + ":" + auctionId + ":ENDED:" + title;
    if (SHOWN_KEYS.add(key)) {
      AlertHelper.showInfo(title, message);
    }
  }

  private static Long getLong(JsonObject data, String key) {
    try {
      if (!data.has(key) || data.get(key).isJsonNull()) {
        return null;
      }
      return data.get(key).getAsLong();
    } catch (Exception ignored) {
      return null;
    }
  }

  private static String getString(JsonObject data, String key) {
    try {
      if (!data.has(key) || data.get(key).isJsonNull()) {
        return null;
      }
      return data.get(key).getAsString();
    } catch (Exception ignored) {
      return null;
    }
  }

  private static boolean containsUserId(JsonObject data, String key, Long userId) {
    if (userId == null) {
      return false;
    }
    try {
      if (!data.has(key) || !data.get(key).isJsonArray()) {
        return false;
      }
      JsonArray array = data.getAsJsonArray(key);
      for (JsonElement element : array) {
        if (element != null && !element.isJsonNull() && userId.equals(element.getAsLong())) {
          return true;
        }
      }
    } catch (Exception ignored) {
      return false;
    }
    return false;
  }

  private static String safeText(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }
}
