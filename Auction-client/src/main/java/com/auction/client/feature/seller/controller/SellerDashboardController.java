package com.auction.client.feature.seller.controller;

import com.auction.client.core.event.AppEvent;
import com.auction.client.core.event.EventBus;
import com.auction.client.core.event.EventListener;
import com.auction.client.core.event.EventType;
import com.auction.client.core.security.AccessGuard;
import com.auction.client.core.session.UserSession;
import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.DisposableController;
import com.auction.client.core.ui.FxAsync;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.client.core.util.MoneyFormat;
import com.auction.client.feature.seller.service.SellerService;
import com.auction.client.feature.seller.service.SellerServiceImpl;
import com.auction.client.feature.auction.controller.AuctionEndNotificationHelper;
import com.auction.client.feature.seller.validator.SellerItemFormValidator;
import com.auction.client.feature.wallet.ui.WalletDialog;
import com.auction.shared.dto.seller.SellerItemDto;
import com.auction.shared.dto.category.CategoryDto;
import com.auction.validation.ValidationResult;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;

/** Seller Dashboard: quản lý sản phẩm đấu giá của Seller. */
public class SellerDashboardController implements Initializable, DisposableController {

    private final SellerService sellerService = new SellerServiceImpl();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty categoriesLoading = new SimpleBooleanProperty(false);
    private final ObservableList<CategoryOption> categoryOptions = FXCollections.observableArrayList();
    private final EventListener connectionLostListener = this::onConnectionLost;
    private final EventListener statusChangedListener = this::onAuctionStatusChanged;

    private Timeline statusAutoRefreshTimer;
    private boolean disposed = false;

    /** Định dạng dễ nhập cho người dùng Việt Nam: 31/05/2026 20:30. */
    private static final DateTimeFormatter USER_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("d/M/yyyy H:mm");

    /** Định dạng hiển thị trong bảng để Seller phân biệt rõ giờ bắt đầu/kết thúc. */
    private static final DateTimeFormatter TABLE_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<SellerItemDto> tblItems;
    @FXML private TableColumn<SellerItemDto, Long> colId;
    @FXML private TableColumn<SellerItemDto, String> colName;
    @FXML private TableColumn<SellerItemDto, String> colCategory;
    @FXML private TableColumn<SellerItemDto, BigDecimal> colStartPrice;
    @FXML private TableColumn<SellerItemDto, BigDecimal> colCurrentPrice;
    @FXML private TableColumn<SellerItemDto, String> colStatus;
    @FXML private TableColumn<SellerItemDto, LocalDateTime> colStartTime;
    @FXML private TableColumn<SellerItemDto, LocalDateTime> colEndTime;

    @FXML private Button btnRefresh;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            AccessGuard.requireAnyRole(com.auction.shared.domain.UserRole.SELLER, com.auction.shared.domain.UserRole.ADMIN);
        } catch (Exception ex) {
            AlertHelper.showException("Không có quyền", ex);
            SceneNavigator.switchScene(ScenePaths.HOME);
            return;
        }
        colId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        colCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (colStartTime != null) {
            colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
            colStartTime.setCellFactory(tc -> dateTimeCell());
        }
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colEndTime.setCellFactory(tc -> dateTimeCell());

        colStartPrice.setCellFactory(tc -> moneyCell());
        colCurrentPrice.setCellFactory(tc -> moneyCell());

        tblItems.setPlaceholder(new Label("Chưa có sản phẩm. Nhấn Refresh để tải."));

        loadingIndicator.visibleProperty().bind(loading);
        btnRefresh.disableProperty().bind(loading);
        btnAdd.disableProperty().bind(loading
                .or(categoriesLoading)
                .or(Bindings.isEmpty(categoryOptions))
                .or(Bindings.createBooleanBinding(() -> !UserSession.getInstance().isLoggedIn())));
        btnEdit.disableProperty().bind(loading.or(tblItems.getSelectionModel().selectedItemProperty().isNull()));
        btnDelete.disableProperty().bind(loading.or(tblItems.getSelectionModel().selectedItemProperty().isNull()));
        tblItems.disableProperty().bind(loading);

        EventBus.getInstance().subscribe(EventType.CONNECTION_LOST, connectionLostListener);
        EventBus.getInstance().subscribe(EventType.AUCTION_STATUS_CHANGED, statusChangedListener);
        startStatusAutoRefreshTimer();
        loadCategoriesAsync();
        loadMyItemsAsync();
    }


    /**
     * Seller Dashboard không đứng trong từng auction room như màn Live Bidding,
     * nên không thể chỉ phụ thuộc vào nút Refresh. Timer này tự kiểm tra các mốc
     * start/end đang hiển thị và gọi lại server khi đến thời điểm cần đổi trạng thái.
     */
    private void startStatusAutoRefreshTimer() {
        if (statusAutoRefreshTimer != null) {
            statusAutoRefreshTimer.stop();
        }
        statusAutoRefreshTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshWhenVisibleStatusBoundaryReached()));
        statusAutoRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        statusAutoRefreshTimer.play();
    }

    private void refreshWhenVisibleStatusBoundaryReached() {
        if (disposed || loading.get() || tblItems == null || tblItems.getItems().isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean shouldRefresh = tblItems.getItems().stream().anyMatch(item -> isStatusBoundaryReached(item, now));
        if (shouldRefresh) {
            loadMyItemsAsync();
        }
    }

    private boolean isStatusBoundaryReached(SellerItemDto item, LocalDateTime now) {
        if (item == null || now == null) {
            return false;
        }

        String status = item.getStatus() == null ? "" : item.getStatus().trim().toUpperCase();
        LocalDateTime startTime = item.getStartTime();
        LocalDateTime endTime = item.getEndTime();

        if ("SCHEDULED".equals(status) && startTime != null && !startTime.isAfter(now)) {
            return true;
        }
        return "ACTIVE".equals(status) && endTime != null && !endTime.isAfter(now);
    }

    private void onAuctionStatusChanged(AppEvent event) {
        if (disposed || loading.get()) {
            return;
        }
        Platform.runLater(() -> {
            AuctionEndNotificationHelper.showIfEnded(event, null, false);
            loadMyItemsAsync();
        });
    }

    private TableCell<SellerItemDto, LocalDateTime> dateTimeCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? "" : TABLE_DATE_TIME_FORMAT.format(value));
            }
        };
    }

    private TableCell<SellerItemDto, BigDecimal> moneyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? "" : MoneyFormat.grouped(value));
            }
        };
    }

    @FXML
    public void handleBack() {
        dispose();
        SceneNavigator.switchScene(ScenePaths.HOME);
    }

    @FXML
    public void handleOpenWallet() {
        WalletDialog.showWallet();
    }

    @FXML
    public void handleRefresh() {
        loadCategoriesAsync();
        loadMyItemsAsync();
    }

    @FXML
    public void handleAdd() {
        Optional<SellerItemDto> created = showItemFormDialog(null);
        created.ifPresent(item -> runMutation(
                () -> sellerService.createItem(item),
                "Đã tạo sản phẩm."
        ));
    }

    @FXML
    public void handleEdit() {
        SellerItemDto selected = tblItems.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Optional<SellerItemDto> edited = showItemFormDialog(selected);
        edited.ifPresent(item -> {
            item.setItemId(selected.getItemId());
            item.setAuctionId(selected.getAuctionId());
            runMutation(() -> sellerService.updateItem(item), "Đã cập nhật sản phẩm.");
        });
    }

    @FXML
    public void handleDelete() {
        SellerItemDto selected = tblItems.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy phiên");
        confirm.setHeaderText("Hủy phiên đấu giá: " + safe(selected.getName()));
        confirm.setContentText("Hệ thống sẽ chuyển phiên sang trạng thái CANCELLED, không xóa cứng dữ liệu để giữ lịch sử.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        runMutation(() -> sellerService.deleteItem(selected), "Đã hủy phiên đấu giá.");
    }

    private void runMutation(ThrowingAction action, String successMessage) {
        loading.set(true);
        FxAsync.run(
                () -> {
                    try {
                        action.run();
                        return null;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                v -> {
                    loadMyItemsAsync();
                    AlertHelper.showInfo("Thành công", successMessage);
                },
                err -> AlertHelper.showException("Lỗi", err),
                () -> loading.set(false)
        );
    }

    private void loadMyItemsAsync() {
        try {
            AccessGuard.requireAnyRole(com.auction.shared.domain.UserRole.SELLER, com.auction.shared.domain.UserRole.ADMIN);
        } catch (Exception ex) {
            tblItems.getItems().clear();
            AlertHelper.showException("Không có quyền", ex);
            return;
        }

        loading.set(true);
        FxAsync.run(
                () -> {
                    try {
                        return sellerService.listMyItems();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                items -> tblItems.getItems().setAll(items),
                err -> {
                    tblItems.getItems().clear();
                    AlertHelper.showException("Không thể tải danh sách", err);
                },
                () -> loading.set(false)
        );
    }

    private void loadCategoriesAsync() {
        categoriesLoading.set(true);
        FxAsync.run(
                () -> {
                    try {
                        return sellerService.listCategories()
                                .stream()
                                .map(this::toCategoryOption)
                                .toList();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                categories -> categoryOptions.setAll(categories),
                err -> {
                    categoryOptions.clear();
                    AlertHelper.showException("Không thể tải danh mục", err);
                },
                () -> categoriesLoading.set(false)
        );
    }

    private CategoryOption toCategoryOption(CategoryDto dto) {
        // UI chỉ hiển thị tên loại sản phẩm người dùng hiểu được.
        // categoryId/slug vẫn được giữ trong DTO/service, không bắt Seller nhìn mã kỹ thuật.
        String displayName = dto.getCategoryName();
        return new CategoryOption(dto.getCategoryId(), displayName == null ? "" : displayName.trim());
    }

    private Optional<SellerItemDto> showItemFormDialog(SellerItemDto existing) {
        Dialog<SellerItemDto> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm sản phẩm" : "Sửa sản phẩm");
        dialog.setHeaderText(existing == null ? "Nhập thông tin sản phẩm" : "Cập nhật thông tin sản phẩm");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfName = new TextField(existing == null ? "" : safe(existing.getName()));
        TextArea taDesc = new TextArea(existing == null ? "" : safe(existing.getDescription()));
        taDesc.setPrefRowCount(3);
        LocalDateTime defaultStartTime = LocalDateTime.now().withSecond(0).withNano(0).plusMinutes(5);
        LocalDateTime defaultEndTime = defaultStartTime.plusDays(1);

        if (categoryOptions.isEmpty()) {
            AlertHelper.showError("Chưa có danh mục", "Loại sản phẩm chưa tải được từ server/database. Hãy bấm Refresh hoặc kiểm tra bảng categories.");
            return Optional.empty();
        }

        ComboBox<CategoryOption> cbCategory = new ComboBox<>();
        cbCategory.setItems(FXCollections.observableArrayList(categoryOptions));
        cbCategory.setMaxWidth(Double.MAX_VALUE);
        selectCategory(cbCategory, existing == null ? 0 : existing.getCategoryId());

        TextField tfStartPrice = new TextField(existing == null ? "" : MoneyFormat.plain(existing.getStartPrice()));
        TextField tfStartTime = new TextField(formatDateTime(existing == null || existing.getStartTime() == null ? defaultStartTime : existing.getStartTime()));
        TextField tfEndTime = new TextField(formatDateTime(existing == null || existing.getEndTime() == null ? defaultEndTime : existing.getEndTime()));
        tfName.setPromptText("VD: Tranh sơn mài / Xe máy cũ / Thiết bị điện tử");
        taDesc.setPromptText("Mô tả tình trạng, phụ kiện kèm theo, bảo hành...");
        tfStartPrice.setPromptText("VD: 5000000");
        tfStartTime.setPromptText("VD: 31/05/2026 20:30");
        tfEndTime.setPromptText("VD: 01/06/2026 20:30");

        Label timeHint = new Label("Định dạng giờ: dd/MM/yyyy HH:mm. Ví dụ: 31/05/2026 20:30. Giờ bắt đầu phải trước giờ kết thúc; giờ kết thúc phải lớn hơn hiện tại. Nếu giờ bắt đầu đã qua nhưng giờ kết thúc còn ở tương lai, phiên sẽ được mở ACTIVE ngay.");
        timeHint.setWrapText(true);
        timeHint.setStyle("-fx-text-fill: #607D8B; -fx-font-size: 11px;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        GridPane.setHgrow(tfName, Priority.ALWAYS);
        GridPane.setHgrow(taDesc, Priority.ALWAYS);
        GridPane.setHgrow(cbCategory, Priority.ALWAYS);
        GridPane.setHgrow(tfStartPrice, Priority.ALWAYS);
        GridPane.setHgrow(tfStartTime, Priority.ALWAYS);
        GridPane.setHgrow(tfEndTime, Priority.ALWAYS);
        grid.addRow(0, new Label("Tên sản phẩm"), tfName);
        grid.addRow(1, new Label("Mô tả"), taDesc);
        grid.addRow(2, new Label("Loại sản phẩm"), cbCategory);
        grid.addRow(3, new Label("Giá khởi điểm"), tfStartPrice);
        grid.addRow(4, new Label("Bắt đầu"), tfStartTime);
        grid.addRow(5, new Label("Kết thúc"), tfEndTime);
        grid.add(timeHint, 0, 6, 2, 1);
        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateSellerForm(tfName, taDesc, cbCategory, tfStartPrice, tfStartTime, tfEndTime, existing == null)) {
                /*
                 * JavaFX Dialog closes on OK by default. Consume the OK event when
                 * validation fails so the seller keeps the values and can fix them.
                 */
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            LocalDateTime startTime;
            LocalDateTime endTime;
            try {
                startTime = parseDateTimeOrNull(tfStartTime.getText(), "StartTime");
                endTime = parseDateTimeOrNull(tfEndTime.getText(), "EndTime");
            } catch (IllegalArgumentException ex) {
                AlertHelper.showError("Sai dữ liệu", ex.getMessage());
                return null;
            }

            ValidationResult result = SellerItemFormValidator.validate(
                    tfName.getText(),
                    taDesc.getText(),
                    tfStartPrice.getText(),
                    startTime,
                    endTime,
                    existing == null
            );
            if (!result.valid()) {
                AlertHelper.showError("Dữ liệu chưa hợp lệ", firstError(result));
                return null;
            }

            SellerItemDto dto = new SellerItemDto();
            if (existing != null) {
                dto.setItemId(existing.getItemId());
                dto.setAuctionId(existing.getAuctionId());
            }
            dto.setName(tfName.getText().trim());
            dto.setDescription(taDesc.getText() == null ? "" : taDesc.getText().trim());
            CategoryOption selectedCategory = cbCategory.getValue();
            if (selectedCategory == null) {
                AlertHelper.showError("Sai dữ liệu", "Vui lòng chọn loại sản phẩm.");
                return null;
            }
            dto.setCategoryId(selectedCategory.id());
            dto.setCategoryName(selectedCategory.name());
            dto.setStartPrice(MoneyFormat.parse(tfStartPrice.getText()));
            dto.setStartTime(startTime);
            dto.setEndTime(endTime);
            return dto;
        });

        return dialog.showAndWait();
    }

    private boolean validateSellerForm(TextField tfName,
                                       TextArea taDesc,
                                       ComboBox<CategoryOption> cbCategory,
                                       TextField tfStartPrice,
                                       TextField tfStartTime,
                                       TextField tfEndTime,
                                       boolean creatingNewItem) {
        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            parseCategoryOption(cbCategory);
            startTime = parseDateTimeOrNull(tfStartTime.getText(), "StartTime");
            endTime = parseDateTimeOrNull(tfEndTime.getText(), "EndTime");
        } catch (IllegalArgumentException ex) {
            AlertHelper.showError("Sai dữ liệu", ex.getMessage());
            return false;
        }

        ValidationResult result = SellerItemFormValidator.validate(
                tfName.getText(),
                taDesc.getText(),
                tfStartPrice.getText(),
                startTime,
                endTime,
                creatingNewItem
        );
        if (!result.valid()) {
            AlertHelper.showError("Dữ liệu chưa hợp lệ", firstError(result));
            return false;
        }
        return true;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : USER_DATE_TIME_FORMAT.format(value);
    }

    private LocalDateTime parseDateTimeOrNull(String raw, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String normalized = raw.trim();
        try {
            // Cách nhập chính cho UI: 31/05/2026 20:30 hoặc 1/6/2026 8:05.
            return LocalDateTime.parse(normalized, USER_DATE_TIME_FORMAT);
        } catch (DateTimeParseException ignored) {
            // Vẫn giữ tương thích với dữ liệu/ghi chú cũ dạng ISO: 2026-05-26T20:00:00.
            try {
                return LocalDateTime.parse(normalized);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException(
                        fieldName + " phải theo dạng dd/MM/yyyy HH:mm, ví dụ 31/05/2026 20:30"
                );
            }
        }
    }

    private CategoryOption parseCategoryOption(ComboBox<CategoryOption> cbCategory) {
        CategoryOption selected = cbCategory == null ? null : cbCategory.getValue();
        if (selected == null || selected.id() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn loại sản phẩm.");
        }
        return selected;
    }

    private void selectCategory(ComboBox<CategoryOption> comboBox, long categoryId) {
        if (comboBox == null || comboBox.getItems().isEmpty()) {
            return;
        }
        comboBox.getItems().stream()
                .filter(option -> option.id() == categoryId)
                .findFirst()
                .ifPresentOrElse(
                        comboBox.getSelectionModel()::select,
                        () -> comboBox.getSelectionModel().selectFirst()
                );
    }

    private String firstError(ValidationResult result) {
        return result.errors().values().stream().findFirst().orElse("Dữ liệu chưa hợp lệ");
    }

    private void onConnectionLost(AppEvent event) {
        loading.set(true);
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        EventBus.getInstance().unsubscribe(EventType.CONNECTION_LOST, connectionLostListener);
        EventBus.getInstance().unsubscribe(EventType.AUCTION_STATUS_CHANGED, statusChangedListener);
        if (statusAutoRefreshTimer != null) {
            statusAutoRefreshTimer.stop();
            statusAutoRefreshTimer = null;
        }
        if (loadingIndicator != null) {
            loadingIndicator.visibleProperty().unbind();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record CategoryOption(long id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }
}
