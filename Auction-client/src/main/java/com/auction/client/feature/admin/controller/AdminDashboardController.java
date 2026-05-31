package com.auction.client.feature.admin.controller;

import com.auction.client.core.event.AppEvent;
import com.auction.client.core.event.EventBus;
import com.auction.client.core.event.EventListener;
import com.auction.client.core.event.EventType;
import com.auction.client.core.event.NetworkEventPayload;
import com.auction.client.core.security.AccessGuard;
import com.auction.client.core.session.ClientSessionManager;
import com.auction.client.core.session.UserSession;
import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.DisposableController;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.shared.domain.UserRole;
import com.google.gson.JsonElement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Admin dashboard (tối thiểu) cho CLIENT.
 *
 * <p>Lưu ý: - Không đoán nghiệp vụ admin phía server. Màn hình này chủ yếu giúp debug
 * realtime/event. - Khi team server chốt thêm API admin, ta sẽ bổ sung AdminService theo
 * ActionConstants mà không phải refactor UI nhiều.
 */
public class AdminDashboardController implements DisposableController {

  @FXML private Label lblSessionInfo;
  @FXML private ListView<String> lvEventLog;

  private final EventBus eventBus = EventBus.getInstance();
  private EventListener networkEventListener;
  private boolean disposed;

  private static final DateTimeFormatter TIME_FMT =
      DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

  @FXML
  public void initialize() {
    try {
      AccessGuard.requireRole(UserRole.ADMIN);
    } catch (Exception ex) {
      AlertHelper.showException("Không có quyền", ex);
      SceneNavigator.switchScene(ScenePaths.HOME);
      return;
    }

    // Session info
    UserSession session = UserSession.getInstance();
    lblSessionInfo.setText(
        session.isLoggedIn()
            ? "Session: " + session.displayName() + " (" + session.getRole() + ")"
            : "Session: (not logged in)");

    // Listen network events to help admin monitor realtime behaviour
    networkEventListener = this::onNetworkEvent;
    eventBus.subscribe(EventType.NETWORK_EVENT, networkEventListener);
    // Domain-level mapped events also logged here for convenience
    eventBus.subscribe(EventType.BID_UPDATED, networkEventListener);
    eventBus.subscribe(EventType.AUCTION_STATUS_CHANGED, networkEventListener);
    eventBus.subscribe(EventType.CONNECTION_LOST, networkEventListener);

    appendLog("Admin dashboard ready. Listening for server events...");
  }

  private void onNetworkEvent(AppEvent event) {
    Platform.runLater(
        () -> {
          String now = TIME_FMT.format(Instant.now());
          String line = "[" + now + "] " + formatEvent(event);
          lvEventLog.getItems().add(0, line);
        });
  }

  private String formatEvent(AppEvent event) {
    if (event.type() == EventType.CONNECTION_LOST) {
      return "CONNECTION_LOST: " + event.payload();
    }

    Object payload = event.payload();
    if (payload instanceof NetworkEventPayload nep) {
      String action = nep.getAction();
      JsonElement data = nep.getData();
      return event.type() + " action=" + action + " data=" + safeJson(data);
    }

    return event.type() + " payload=" + payload;
  }

  private String safeJson(JsonElement el) {
    if (el == null || el.isJsonNull()) return "null";
    String s = el.toString();
    // keep log readable
    if (s.length() > 200) {
      return s.substring(0, 200) + "...";
    }
    return s;
  }

  private void appendLog(String message) {
    Platform.runLater(() -> lvEventLog.getItems().add(0, message));
  }

  @FXML
  public void handleClearLog() {
    lvEventLog.getItems().clear();
    appendLog("Log cleared.");
  }

  @FXML
  public void handleOpenAuctions() {
    dispose();
    SceneNavigator.switchScene(ScenePaths.AUCTION_LIST);
  }

  @FXML
  public void handleBack() {
    dispose();
    SceneNavigator.switchScene(ScenePaths.HOME);
  }

  @FXML
  public void handleLogout() {
    dispose();
    ClientSessionManager.logoutToLogin();
  }

  @Override
  public void dispose() {
    if (disposed) {
      return;
    }
    disposed = true;
    if (networkEventListener != null) {
      eventBus.unsubscribe(EventType.NETWORK_EVENT, networkEventListener);
      eventBus.unsubscribe(EventType.BID_UPDATED, networkEventListener);
      eventBus.unsubscribe(EventType.AUCTION_STATUS_CHANGED, networkEventListener);
      eventBus.unsubscribe(EventType.CONNECTION_LOST, networkEventListener);
      networkEventListener = null;
    }
  }
}
