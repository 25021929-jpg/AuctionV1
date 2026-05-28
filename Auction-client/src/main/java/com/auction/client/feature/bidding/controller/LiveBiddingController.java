package com.auction.client.feature.bidding.controller;

import com.auction.client.core.error.ErrorHandler;
import com.auction.client.core.security.AccessGuard;
import com.auction.client.core.event.AppEvent;
import com.auction.client.core.event.EventBus;
import com.auction.client.core.event.EventListener;
import com.auction.client.core.event.EventType;
import com.auction.client.core.event.NetworkEventPayload;
import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.DisposableController;
import com.auction.client.core.ui.FxAsync;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.client.feature.auction.controller.AuctionDetailController;
import com.auction.shared.dto.auction.AuctionDetailDto;
import java.math.BigDecimal;
import com.auction.client.feature.auction.service.AuctionService;
import com.auction.client.feature.auction.service.AuctionServiceImpl;
import com.auction.client.feature.bidding.service.BidService;
import com.auction.client.feature.bidding.service.BidServiceImpl;
import com.auction.shared.domain.AuctionStatus;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Màn hình đấu giá trực tiếp.
 *
 * Realtime update: nhận EVENT_BID_UPDATED từ server (Socket push) và cập nhật UI.
 */
public class LiveBiddingController implements DisposableController {

    @FXML private Label lblCurrentPrice;
    @FXML private TextField bidAmountField;
    @FXML private Button btnPlaceBid;
    @FXML private ProgressIndicator placingIndicator;
    @FXML private LineChart<String, Number> priceChart;

    private final XYChart.Series<String, Number> series = new XYChart.Series<>();

    /** Giới hạn số điểm trên chart để tránh phình bộ nhớ khi chạy lâu. */
    private static final int MAX_CHART_POINTS = 120;

    /** Auction đang xem (được truyền từ màn detail). */
    private Long auctionId;

    private final BidService bidService = new BidServiceImpl();

    private final AuctionService auctionService = new AuctionServiceImpl();

    private boolean initialized = false;
    private boolean disposed = false;

    /** Listener để unsubscribe khi rời màn hình. */
    private final EventListener bidUpdatedListener = this::onBidUpdatedEvent;
    private final EventListener statusChangedListener = this::onAuctionStatusChangedEvent;
    private final EventListener connectionLostListener = this::onConnectionLost;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    /**
     * Giá hiện tại mà UI đang hiển thị.
     *
     * <p>Dùng để validate tối thiểu phía client: bid phải lớn hơn giá hiện tại.
     * Server vẫn là nơi kiểm tra cuối cùng.
     */
    private BigDecimal displayedCurrentPrice;

    /** Trạng thái phiên đấu giá (từ detail hoặc event). */
    private AuctionStatus auctionStatus = null;

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
        if (initialized && auctionId != null) {
            trySubscribe();
            loadInitialDetail();
        }
    }

    @FXML
    public void initialize() {
        try {
            AccessGuard.requireLogin();
        } catch (Exception ex) {
            AlertHelper.showException("Chưa đăng nhập", ex);
            SceneNavigator.switchScene(ScenePaths.LOGIN);
            return;
        }

        priceChart.getData().add(series);
        lblCurrentPrice.setText("-");

        initialized = true;

        // Nếu auctionId được set trước (hiếm), subscribe ngay.
        if (auctionId != null) {
            trySubscribe();
            loadInitialDetail();
        }

        // Subscribe event đã map (EventType.BID_UPDATED)
        EventBus.getInstance().subscribe(EventType.BID_UPDATED, bidUpdatedListener);
        EventBus.getInstance().subscribe(EventType.AUCTION_STATUS_CHANGED, statusChangedListener);
        EventBus.getInstance().subscribe(EventType.CONNECTION_LOST, connectionLostListener);

        // Disable nút đặt giá nếu input không hợp lệ.
        if (bidAmountField != null) {
            bidAmountField.textProperty().addListener((obs, o, n) -> updatePlaceBidButtonState());
        }
        updatePlaceBidButtonState();
    }

    private void trySubscribe() {
        try {
            bidService.subscribeAuction(auctionId);
        } catch (Exception ignored) {
            // best-effort: nếu server chưa hỗ trợ subscribe, vẫn nhận event broadcast.
        }
    }

    private void loadInitialDetail() {
        if (auctionId == null) return;
        FxAsync.run(
                () -> {
                    try {
                        return auctionService.fetchAuctionDetail(auctionId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                dto -> {
                    if (dto == null) return;
                    Platform.runLater(() -> applyDetail(dto));
                },
                err -> {
                    // Nếu không load được detail thì vẫn cho phép xem chart; việc bid sẽ do server quyết định.
                    // Không show alert ở đây để tránh spam khi server chưa hỗ trợ action.
                },
                () -> {}
        );
    }

    private void applyDetail(AuctionDetailDto dto) {
        // Set current price nếu có.
        BigDecimal price = dto.getCurrentPrice();
        lblCurrentPrice.setText(price == null ? "-" : price.toPlainString());
        displayedCurrentPrice = price;
        auctionStatus = dto.getStatus();
        updateBiddingAvailability();
    }

    private void onAuctionStatusChangedEvent(AppEvent event) {
        if (!(event.payload() instanceof NetworkEventPayload payload)) return;
        JsonElement dataEl = payload.getData();
        if (dataEl == null || !dataEl.isJsonObject()) return;
        JsonObject data = dataEl.getAsJsonObject();

        Long evAuctionId = tryGetLong(data, "auctionId");
        String status = null;
        try {
            if (data.has("status") && !data.get("status").isJsonNull()) {
                status = data.get("status").getAsString();
            }
        } catch (Exception ignored) {
            status = null;
        }

        if (evAuctionId == null || status == null) return;
        if (auctionId != null && !auctionId.equals(evAuctionId)) return;

        final AuctionStatus finalStatus = AuctionStatus.fromString(status);
        Platform.runLater(() -> {
            auctionStatus = finalStatus;
            updateBiddingAvailability();
        });
    }

    private void updateBiddingAvailability() {
        boolean canBid = (auctionStatus == null) || auctionStatus.isBiddable();
        if (bidAmountField != null) {
            bidAmountField.setDisable(!canBid);
        }
        updatePlaceBidButtonState();
    }

    private void onConnectionLost(AppEvent event) {
        // Tránh spam alert: MainClient đã show thông báo mất kết nối.
        Platform.runLater(() -> {
            if (bidAmountField != null) bidAmountField.setDisable(true);
            if (btnPlaceBid != null) btnPlaceBid.setDisable(true);
        });
    }

    private void onBidUpdatedEvent(AppEvent event) {
        if (!(event.payload() instanceof NetworkEventPayload payload)) return;

        JsonElement dataEl = payload.getData();
        if (dataEl == null || !dataEl.isJsonObject()) return;
        JsonObject data = dataEl.getAsJsonObject();

        // Payload tối thiểu kỳ vọng (có thể thay đổi theo server):
        // { "auctionId": 1, "currentPrice": 123.0, "timestamp": 1710000000000 }
        // - Nếu thiếu field -> bỏ qua an toàn.

        Long evAuctionId = tryGetLong(data, "auctionId");
        BigDecimal currentPrice = tryGetBigDecimal(data, "currentPrice");
        Long ts = tryGetLong(data, "timestamp");

        if (evAuctionId == null || currentPrice == null) return;
        if (auctionId != null && !auctionId.equals(evAuctionId)) return; // không phải phiên đang xem

        String x = (ts != null)
                ? TIME_FMT.format(Instant.ofEpochMilli(ts))
                : TIME_FMT.format(Instant.now());

        Platform.runLater(() -> {
            lblCurrentPrice.setText(currentPrice.toPlainString());
            displayedCurrentPrice = currentPrice;
            addChartPoint(x, currentPrice);
        });
    }

    private static Long tryGetLong(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
            return obj.get(key).getAsLong();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static BigDecimal tryGetBigDecimal(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
            return obj.get(key).getAsBigDecimal();
        } catch (Exception ignored) {
            return null;
        }
    }

    @FXML
    public void handlePlaceBid() {
        String text = bidAmountField.getText();
        if (text == null || text.isBlank()) {
            AlertHelper.showError("Thiếu dữ liệu", "Vui lòng nhập giá muốn đặt.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(text.trim());
        } catch (NumberFormatException ex) {
            AlertHelper.showError("Sai định dạng", "Giá phải là số.");
            return;
        }

        if (amount.signum() <= 0) {
            AlertHelper.showError("Giá đấu không hợp lệ", "Giá đặt phải lớn hơn 0.");
            return;
        }

        if (auctionId == null) {
            AlertHelper.showError("Thiếu dữ liệu", "Không xác định được phiên đấu giá.");
            return;
        }

        if (auctionStatus != null && !auctionStatus.isBiddable()) {
            AlertHelper.showError("Phiên đã đóng", "Không thể đặt giá khi phiên không còn mở.");
            return;
        }

        // Validate tối thiểu theo yêu cầu đề bài: bid phải cao hơn giá hiện tại.
        // (Tránh gửi request vô ích; server vẫn kiểm tra lại.)
        BigDecimal current = getDisplayedCurrentPriceSafe();
        if (current != null && amount.compareTo(current) <= 0) {
            AlertHelper.showError(
                    "Giá đấu không hợp lệ",
                    "Giá đặt phải lớn hơn giá hiện tại (" + current.toPlainString() + ")."
            );
            return;
        }

        setPlacing(true);
        FxAsync.run(
                () -> {
                    try {
                        bidService.placeBid(auctionId, amount);
                        return amount;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                placedAmount -> {
                    // Không optimistic-update giá bằng số người dùng vừa nhập, vì auto-bid/concurrency
                    // có thể khiến giá thực tế khác. Sau response thành công, chờ EVENT_BID_UPDATED;
                    // nếu event chưa tới thì reload detail một lần làm fallback an toàn.
                    Platform.runLater(() -> {
                        bidAmountField.clear();
                        loadInitialDetail();
                    });
                },
                err -> AlertHelper.showError("Lỗi đặt giá", ErrorHandler.getUserMessage(err)),
                () -> setPlacing(false)
        );
    }

    private BigDecimal getDisplayedCurrentPriceSafe() {
        if (displayedCurrentPrice != null) {
            return displayedCurrentPrice;
        }
        if (lblCurrentPrice == null) {
            return null;
        }
        String text = lblCurrentPrice.getText();
        if (text == null || text.isBlank() || "-".equals(text.trim())) {
            return null;
        }
        try {
            displayedCurrentPrice = new BigDecimal(text.trim());
            return displayedCurrentPrice;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setPlacing(boolean placing) {
        if (placingIndicator != null) {
            placingIndicator.setVisible(placing);
        }
        updatePlaceBidButtonState();
        if (bidAmountField != null) {
            bidAmountField.setDisable(placing);
        }
    }

    private void updatePlaceBidButtonState() {
        if (btnPlaceBid == null) return;

        // Nếu phiên không cho phép bid (FINISHED/PAID/CANCELED), luôn disable.
        if (bidAmountField != null && bidAmountField.isDisable()) {
            btnPlaceBid.setDisable(true);
            return;
        }

        boolean placing = placingIndicator != null && placingIndicator.isVisible();
        if (placing) {
            btnPlaceBid.setDisable(true);
            return;
        }

        String text = bidAmountField != null ? bidAmountField.getText() : null;
        if (text == null || text.isBlank()) {
            btnPlaceBid.setDisable(true);
            return;
        }

        try {
            BigDecimal value = new BigDecimal(text.trim());
            BigDecimal current = getDisplayedCurrentPriceSafe();
            boolean invalidAmount = value.signum() <= 0;
            boolean notHigherThanCurrent = current != null && value.compareTo(current) <= 0;
            btnPlaceBid.setDisable(invalidAmount || notHigherThanCurrent);
        } catch (Exception ignored) {
            btnPlaceBid.setDisable(true);
        }
    }

    private void addChartPoint(String timeLabel, BigDecimal price) {
        series.getData().add(new XYChart.Data<>(timeLabel, price));
        // giới hạn số điểm
        if (series.getData().size() > MAX_CHART_POINTS) {
            int removeCount = series.getData().size() - MAX_CHART_POINTS;
            series.getData().remove(0, removeCount);
        }
    }

    @FXML
    public void handleBack() {
        dispose();
        // Quay lại màn hình chi tiết và truyền lại auctionId.
        SceneNavigator.switchScene(ScenePaths.AUCTION_DETAIL, controller -> {
            if (controller instanceof AuctionDetailController c) {
                c.setAuctionId(auctionId);
            }
        });
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        EventBus.getInstance().unsubscribe(EventType.BID_UPDATED, bidUpdatedListener);
        EventBus.getInstance().unsubscribe(EventType.AUCTION_STATUS_CHANGED, statusChangedListener);
        EventBus.getInstance().unsubscribe(EventType.CONNECTION_LOST, connectionLostListener);
        if (auctionId != null) {
            try {
                bidService.unsubscribeAuction(auctionId);
            } catch (Exception ignored) {
                // best-effort cleanup
            }
        }
    }

}
