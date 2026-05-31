package com.auction.client.feature.auction.controller;

import com.auction.client.core.event.AppEvent;
import com.auction.client.core.event.EventBus;
import com.auction.client.core.event.EventListener;
import com.auction.client.core.event.EventType;
import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.DisposableController;
import com.auction.client.core.ui.FxAsync;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.client.core.util.MoneyFormat;
import com.auction.client.feature.auction.service.AuctionService;
import com.auction.client.feature.auction.service.AuctionServiceImpl;
import com.auction.shared.domain.AuctionStatus;
import com.auction.shared.dto.auction.AuctionSummaryDto;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

public class AuctionListController implements DisposableController {

  @FXML private TableView<AuctionSummaryDto> auctionTable;
  @FXML private TableColumn<AuctionSummaryDto, Long> colId;
  @FXML private TableColumn<AuctionSummaryDto, String> colName;
  @FXML private TableColumn<AuctionSummaryDto, BigDecimal> colPrice;
  @FXML private TableColumn<AuctionSummaryDto, Integer> colTotalBids;
  @FXML private TableColumn<AuctionSummaryDto, LocalDateTime> colStartTime;
  @FXML private TableColumn<AuctionSummaryDto, LocalDateTime> colEndTime;
  @FXML private TableColumn<AuctionSummaryDto, String> colStatus;

  @FXML private Button btnRefresh;
  @FXML private ProgressIndicator loadingIndicator;

  private final AuctionService auctionService = new AuctionServiceImpl();

  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final EventListener connectionLostListener = this::onConnectionLost;
  private final EventListener statusChangedListener = this::onAuctionStatusChanged;
  private Timeline statusAutoRefreshTimer;
  private boolean disposed = false;

  @FXML
  public void initialize() {
    colId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
    colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
    if (colTotalBids != null)
      colTotalBids.setCellValueFactory(new PropertyValueFactory<>("totalBids"));
    if (colStartTime != null) {
      colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
      colStartTime.setCellFactory(tc -> dateTimeCell());
    }
    if (colEndTime != null) {
      colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
      colEndTime.setCellFactory(tc -> dateTimeCell());
    }
    colStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
    colPrice.setCellFactory(
        tc ->
            new javafx.scene.control.TableCell<>() {
              @Override
              protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : MoneyFormat.grouped(v));
              }
            });

    // UX tối thiểu: trạng thái rỗng rõ ràng.
    auctionTable.setPlaceholder(new Label("Chưa có phiên đấu giá. Nhấn Refresh để tải."));

    loadAuctionsAsync();

    EventBus.getInstance().subscribe(EventType.CONNECTION_LOST, connectionLostListener);
    EventBus.getInstance().subscribe(EventType.AUCTION_STATUS_CHANGED, statusChangedListener);
    startStatusAutoRefreshTimer();

    auctionTable.setOnMouseClicked(
        e -> {
          if (e.getClickCount() == 2) {
            AuctionSummaryDto selected = auctionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
              SceneNavigator.switchScene(
                  ScenePaths.AUCTION_DETAIL,
                  controller -> {
                    if (controller instanceof AuctionDetailController c) {
                      c.setAuctionId(selected.getAuctionId());
                    }
                  });
            }
          }
        });
  }

  /** Tự làm mới danh sách khi các phiên đang hiển thị đi qua mốc start/end. */
  private void startStatusAutoRefreshTimer() {
    if (statusAutoRefreshTimer != null) {
      statusAutoRefreshTimer.stop();
    }
    statusAutoRefreshTimer =
        new Timeline(
            new KeyFrame(Duration.seconds(1), event -> refreshWhenVisibleStatusBoundaryReached()));
    statusAutoRefreshTimer.setCycleCount(Timeline.INDEFINITE);
    statusAutoRefreshTimer.play();
  }

  private void refreshWhenVisibleStatusBoundaryReached() {
    if (disposed
        || auctionTable == null
        || auctionTable.isDisabled()
        || auctionTable.getItems().isEmpty()) {
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    boolean shouldRefresh =
        auctionTable.getItems().stream().anyMatch(item -> isStatusBoundaryReached(item, now));
    if (shouldRefresh) {
      loadAuctionsAsync();
    }
  }

  private boolean isStatusBoundaryReached(AuctionSummaryDto item, LocalDateTime now) {
    if (item == null || item.getStatus() == null || now == null) {
      return false;
    }
    AuctionStatus status = item.getStatus();
    if (status == AuctionStatus.SCHEDULED
        && item.getStartTime() != null
        && !item.getStartTime().isAfter(now)) {
      return true;
    }
    return status == AuctionStatus.ACTIVE
        && item.getEndTime() != null
        && !item.getEndTime().isAfter(now);
  }

  private void onAuctionStatusChanged(AppEvent event) {
    if (disposed || auctionTable == null || auctionTable.isDisabled()) {
      return;
    }
    Platform.runLater(
        () -> {
          AuctionEndNotificationHelper.showIfEnded(event, null, false);
          loadAuctionsAsync();
        });
  }

  private javafx.scene.control.TableCell<AuctionSummaryDto, LocalDateTime> dateTimeCell() {
    return new javafx.scene.control.TableCell<>() {
      @Override
      protected void updateItem(LocalDateTime value, boolean empty) {
        super.updateItem(value, empty);
        setText(empty || value == null ? "" : DATE_TIME_FORMAT.format(value));
      }
    };
  }

  @FXML
  public void handleRefresh() {
    loadAuctionsAsync();
  }

  private void setLoading(boolean loading) {
    if (loadingIndicator != null) {
      loadingIndicator.setVisible(loading);
    }
    if (btnRefresh != null) {
      btnRefresh.setDisable(loading);
    }
    if (auctionTable != null) {
      auctionTable.setDisable(loading);
    }
  }

  private void loadAuctionsAsync() {
    setLoading(true);
    FxAsync.run(
        () -> {
          try {
            return auctionService.fetchAuctions();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        },
        auctions -> auctionTable.setItems(FXCollections.observableArrayList(auctions)),
        err -> AlertHelper.showException("Lỗi", err),
        () -> setLoading(false));
  }

  @FXML
  public void handleBack() {
    dispose();
    SceneNavigator.switchScene(ScenePaths.HOME);
  }

  @Override
  public void dispose() {
    disposed = true;
    EventBus.getInstance().unsubscribe(EventType.CONNECTION_LOST, connectionLostListener);
    EventBus.getInstance().unsubscribe(EventType.AUCTION_STATUS_CHANGED, statusChangedListener);
    if (statusAutoRefreshTimer != null) {
      statusAutoRefreshTimer.stop();
      statusAutoRefreshTimer = null;
    }
  }

  private void onConnectionLost(AppEvent event) {
    // MainClient đã hiển thị alert; controller chỉ khóa thao tác để tránh lỗi tiếp theo.
    setLoading(true);
  }
}
