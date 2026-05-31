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
import com.auction.client.feature.auction.controller.AuctionEndNotificationHelper;
import com.auction.shared.dto.auction.AuctionDetailDto;
import java.math.BigDecimal;
import com.auction.client.feature.auction.service.AuctionService;
import com.auction.client.feature.auction.service.AuctionServiceImpl;
import com.auction.client.feature.bidding.service.BidService;
import com.auction.client.feature.bidding.service.BidServiceImpl;
import com.auction.client.core.session.UserSession;
import com.auction.client.feature.wallet.ui.WalletDialog;
import com.auction.client.feature.wallet.service.WalletService;
import com.auction.client.feature.wallet.service.WalletServiceImpl;
import com.auction.shared.domain.AuctionStatus;
import com.auction.shared.dto.bidding.BidResultDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Màn hình đấu giá trực tiếp.
 *
 * Realtime update: nhận EVENT_BID_UPDATED từ server (Socket push) và cập nhật UI.
 */
public class LiveBiddingController implements DisposableController {

    @FXML private Label lblCurrentPrice;
    @FXML private Label lblMinBidStep;
    @FXML private Label lblMinimumBid;
    @FXML private Label lblBalance;
    @FXML private Label lblAuctionStatus;
    @FXML private Label lblStartTime;
    @FXML private Label lblEndTime;
    @FXML private TextField bidAmountField;
    @FXML private Button btnPlaceBid;
    @FXML private ProgressIndicator placingIndicator;
    @FXML private LineChart<String, Number> priceChart;
    @FXML private TableView<BidHistoryRow> bidHistoryTable;
    @FXML private TableColumn<BidHistoryRow, String> colBidTime;
    @FXML private TableColumn<BidHistoryRow, String> colBidder;
    @FXML private TableColumn<BidHistoryRow, String> colBidAmount;
    @FXML private TableColumn<BidHistoryRow, String> colBidWinning;

    private final XYChart.Series<String, Number> series = new XYChart.Series<>();
    private final ObservableList<BidHistoryRow> bidHistoryRows = FXCollections.observableArrayList();

    /** Giới hạn số điểm trên chart để tránh phình bộ nhớ khi chạy lâu. */
    private static final int MAX_CHART_POINTS = 120;
    private static final int MAX_HISTORY_ROWS = 50;

    /** Auction đang xem (được truyền từ màn detail). */
    private Long auctionId;

    private final BidService bidService = new BidServiceImpl();

    private final AuctionService auctionService = new AuctionServiceImpl();
    private final WalletService walletService = new WalletServiceImpl();

    private boolean initialized = false;
    private boolean disposed = false;

    /** Listener để unsubscribe khi rời màn hình. */
    private final EventListener bidUpdatedListener = this::onBidUpdatedEvent;
    private final EventListener statusChangedListener = this::onAuctionStatusChangedEvent;
    private final EventListener connectionLostListener = this::onConnectionLost;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    private static final DateTimeFormatter BID_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final DateTimeFormatter DETAIL_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Giá hiện tại mà UI đang hiển thị.
     *
     * <p>Dùng để validate tối thiểu phía client: bid phải lớn hơn giá hiện tại.
     * Server vẫn là nơi kiểm tra cuối cùng.
     */
    private BigDecimal displayedCurrentPrice;
    private BigDecimal minBidStep = BigDecimal.valueOf(1000);

    /** Trạng thái phiên đấu giá (từ detail hoặc event). */
    private AuctionStatus auctionStatus = null;

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
        if (initialized && auctionId != null) {
            trySubscribe();
            loadInitialDetail();
            loadBidHistory();
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
        setupBidHistoryTable();
        lblCurrentPrice.setText("-");
        refreshBalanceLabel();
        if (lblAuctionStatus != null) lblAuctionStatus.setText("-");
        if (lblStartTime != null) lblStartTime.setText("-");
        if (lblEndTime != null) lblEndTime.setText("-");
        refreshBidHints();

        initialized = true;

        // Nếu auctionId được set trước (hiếm), subscribe ngay.
        if (auctionId != null) {
            trySubscribe();
            loadInitialDetail();
            loadBidHistory();
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

    private void setupBidHistoryTable() {
        if (bidHistoryTable == null) {
            return;
        }
        colBidTime.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().timeText()));
        colBidder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().bidderText()));
        colBidAmount.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().amountText()));
        colBidWinning.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().winningText()));
        bidHistoryTable.setItems(bidHistoryRows);
    }

    private void loadBidHistory() {
        if (auctionId == null) {
            return;
        }
        FxAsync.run(
                () -> {
                    try {
                        return bidService.getBidHistory(auctionId, MAX_HISTORY_ROWS);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                this::applyBidHistory,
                err -> {
                    // Không chặn màn live nếu lịch sử bid chưa tải được.
                    // Server vẫn kiểm tra nghiệp vụ khi người dùng đặt giá.
                },
                () -> {}
        );
    }

    private void applyBidHistory(List<BidResultDto> history) {
        if (history == null) {
            return;
        }
        Platform.runLater(() -> {
            bidHistoryRows.setAll(history.stream()
                    .sorted(Comparator.comparing(BidResultDto::getBidTime,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .map(BidHistoryRow::fromDto)
                    .toList());

            series.getData().clear();
            history.stream()
                    .filter(b -> b.getBidAmount() != null)
                    .sorted(Comparator.comparing(BidResultDto::getBidTime,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .forEach(b -> addChartPoint(formatBidTimeForChart(b.getBidTime()), b.getBidAmount()));
        });
    }

    private void applyDetail(AuctionDetailDto dto) {
        // Set current price nếu có.
        BigDecimal price = dto.getCurrentPrice();
        lblCurrentPrice.setText(price == null ? "-" : price.toPlainString());
        displayedCurrentPrice = price;
        if (dto.getMinBidStep() != null && dto.getMinBidStep().signum() > 0) {
            minBidStep = dto.getMinBidStep();
        }
        refreshBidHints();
        auctionStatus = dto.getStatus();
        if (lblAuctionStatus != null) {
            lblAuctionStatus.setText(auctionStatus == null ? "-" : auctionStatus.name());
        }
        if (lblStartTime != null) {
            lblStartTime.setText(dto.getStartTime() == null ? "-" : DETAIL_TIME_FMT.format(dto.getStartTime()));
        }
        if (lblEndTime != null) {
            lblEndTime.setText(dto.getEndTime() == null ? "-" : DETAIL_TIME_FMT.format(dto.getEndTime()));
        }
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
            if (lblAuctionStatus != null) lblAuctionStatus.setText(finalStatus == null ? "-" : finalStatus.name());
            updateBiddingAvailability();
            if (finalStatus != null && finalStatus.isFinishedLike()) {
                // Sau khi scheduler chốt phiên, tải lại ví để người thắng/seller thấy số dư mới.
                refreshBalanceFromServer();
                AuctionEndNotificationHelper.showIfEnded(event, auctionId, true);
            }
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

        BidResultDto bid = toBidResultDto(data, evAuctionId, currentPrice);

        Platform.runLater(() -> {
            lblCurrentPrice.setText(currentPrice.toPlainString());
            displayedCurrentPrice = currentPrice;
            refreshBidHints();
            prependBidHistoryRow(bid);
            addChartPoint(x, currentPrice);
        });
    }

    private BidResultDto toBidResultDto(JsonObject data, Long eventAuctionId, BigDecimal currentPrice) {
        BidResultDto dto = new BidResultDto();
        dto.setAuctionId(eventAuctionId == null ? 0L : eventAuctionId);
        dto.setAuctionSessionId(eventAuctionId == null ? 0L : eventAuctionId);
        Long bidId = tryGetLong(data, "bidId");
        if (bidId != null) {
            dto.setBidId(bidId);
        }
        Long bidderId = tryGetLong(data, "bidderId");
        if (bidderId != null) {
            dto.setBidderId(bidderId);
        }
        if (data.has("bidderUsername") && !data.get("bidderUsername").isJsonNull()) {
            dto.setBidderUsername(data.get("bidderUsername").getAsString());
        }
        dto.setBidAmount(currentPrice);
        dto.setNewCurrentPrice(currentPrice);
        dto.setIsWinning(true);
        dto.setBidTime(tryGetLocalDateTime(data, "bidTime"));
        return dto;
    }

    private void prependBidHistoryRow(BidResultDto bid) {
        if (bid == null || bidHistoryTable == null) {
            return;
        }
        if (bid.getBidId() > 0) {
            boolean exists = bidHistoryRows.stream().anyMatch(row -> row.bidId() == bid.getBidId());
            if (exists) {
                return;
            }
        }
        bidHistoryRows.add(0, BidHistoryRow.fromDto(bid));
        if (bidHistoryRows.size() > MAX_HISTORY_ROWS) {
            bidHistoryRows.remove(MAX_HISTORY_ROWS, bidHistoryRows.size());
        }
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

    private static LocalDateTime tryGetLocalDateTime(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
            return LocalDateTime.parse(obj.get(key).getAsString());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String formatBidTimeForChart(LocalDateTime value) {
        return value == null ? TIME_FMT.format(Instant.now()) : value.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @FXML
    public void handleOpenWallet() {
        WalletDialog.showWallet();
        refreshBalanceLabel();
        updatePlaceBidButtonState();
    }

    private void refreshBalanceLabel() {
        if (lblBalance != null) {
            lblBalance.setText("Số dư: " + UserSession.getInstance().getBalance().stripTrailingZeros().toPlainString());
        }
    }

    private void refreshBalanceFromServer() {
        FxAsync.run(
                () -> {
                    try {
                        return walletService.getSummary();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                summary -> Platform.runLater(() -> {
                    refreshBalanceLabel();
                    updatePlaceBidButtonState();
                }),
                err -> Platform.runLater(this::refreshBalanceLabel),
                () -> {}
        );
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

        if (amount.compareTo(UserSession.getInstance().getBalance()) > 0) {
            AlertHelper.showError("Số dư không đủ", "Giá đặt không được vượt quá số dư hiện tại. Hãy nạp thêm tiền trước khi đấu giá.");
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

        // Đồng bộ với rule server: giá mới phải >= giá hiện tại + bước giá tối thiểu.
        BigDecimal current = getDisplayedCurrentPriceSafe();
        if (current != null) {
            BigDecimal minimumAcceptableBid = current.add(minBidStep);
            if (amount.compareTo(minimumAcceptableBid) < 0) {
                AlertHelper.showError(
                        "Giá đấu không hợp lệ",
                        "Giá đặt tối thiểu là " + minimumAcceptableBid.toPlainString()
                                + " (giá hiện tại + bước giá " + minBidStep.toPlainString() + ")."
                );
                return;
            }
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

        // Nếu phiên không cho phép bid (SCHEDULED/ENDED/CANCELLED), luôn disable.
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
            BigDecimal minimumAcceptableBid = current == null ? null : current.add(minBidStep);
            boolean invalidAmount = value.signum() <= 0;
            boolean lowerThanMinimum = minimumAcceptableBid != null && value.compareTo(minimumAcceptableBid) < 0;
            boolean greaterThanBalance = value.compareTo(UserSession.getInstance().getBalance()) > 0;
            btnPlaceBid.setDisable(invalidAmount || lowerThanMinimum || greaterThanBalance);
        } catch (Exception ignored) {
            btnPlaceBid.setDisable(true);
        }
    }

    private void refreshBidHints() {
        if (lblMinBidStep != null) {
            lblMinBidStep.setText(minBidStep.toPlainString());
        }
        BigDecimal current = getDisplayedCurrentPriceSafe();
        if (lblMinimumBid != null) {
            lblMinimumBid.setText(current == null ? "-" : current.add(minBidStep).toPlainString());
        }
        if (bidAmountField != null && current != null) {
            bidAmountField.setPromptText("Nhập giá ≥ " + current.add(minBidStep).toPlainString());
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

    /** Row model riêng cho TableView, không để FXML phụ thuộc trực tiếp vào DTO mạng. */
    public static final class BidHistoryRow {
        private final long bidId;
        private final String timeText;
        private final String bidderText;
        private final String amountText;
        private final String winningText;

        private BidHistoryRow(long bidId, String timeText, String bidderText, String amountText, String winningText) {
            this.bidId = bidId;
            this.timeText = timeText;
            this.bidderText = bidderText;
            this.amountText = amountText;
            this.winningText = winningText;
        }

        static BidHistoryRow fromDto(BidResultDto dto) {
            String time = dto.getBidTime() == null ? "-" : BID_TIME_FMT.format(dto.getBidTime());
            String bidder = dto.getBidderUsername() == null || dto.getBidderUsername().isBlank()
                    ? "#" + dto.getBidderId()
                    : dto.getBidderUsername();
            String amount = dto.getBidAmount() == null ? "-" : dto.getBidAmount().toPlainString();
            String status = Boolean.TRUE.equals(dto.getIsWinning()) ? "Đang dẫn đầu" : "Đã ghi";
            return new BidHistoryRow(dto.getBidId(), time, bidder, amount, status);
        }

        long bidId() { return bidId; }
        String timeText() { return timeText; }
        String bidderText() { return bidderText; }
        String amountText() { return amountText; }
        String winningText() { return winningText; }
    }

}
