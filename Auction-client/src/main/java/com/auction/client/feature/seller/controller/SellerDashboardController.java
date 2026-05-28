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
import com.auction.client.feature.seller.validator.SellerItemFormValidator;
import com.auction.shared.dto.seller.SellerItemDto;
import com.auction.validation.ValidationResult;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;

/** Seller Dashboard: quản lý sản phẩm đấu giá của Seller. */
public class SellerDashboardController implements Initializable, DisposableController {

    private final SellerService sellerService = new SellerServiceImpl();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final EventListener connectionLostListener = this::onConnectionLost;

    private boolean disposed = false;

    @FXML private TableView<SellerItemDto> tblItems;
    @FXML private TableColumn<SellerItemDto, Long> colId;
    @FXML private TableColumn<SellerItemDto, String> colName;
    @FXML private TableColumn<SellerItemDto, BigDecimal> colStartPrice;
    @FXML private TableColumn<SellerItemDto, BigDecimal> colCurrentPrice;
    @FXML private TableColumn<SellerItemDto, String> colStatus;
    @FXML private TableColumn<SellerItemDto, LocalDateTime> colEndTime;

    @FXML private Button btnRefresh;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            /*
             * Seller Dashboard chỉ yêu cầu user đã đăng nhập.
             *
             * User mới đăng ký đang được server gán role BIDDER mặc định. Nếu client
             * yêu cầu role SELLER ở đây, chính client tạo rào cản khiến một tài khoản
             * không thể vừa bid vừa sell. Quyền sở hữu item vẫn được server kiểm tra
             * bằng sellerId khi list/update/delete sản phẩm.
             */
            AccessGuard.requireLogin();
        } catch (Exception ex) {
            AlertHelper.showException("Không có quyền", ex);
            SceneNavigator.switchScene(ScenePaths.HOME);
            return;
        }
        colId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        colCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        colStartPrice.setCellFactory(tc -> moneyCell());
        colCurrentPrice.setCellFactory(tc -> moneyCell());

        tblItems.setPlaceholder(new Label("Chưa có sản phẩm. Nhấn Refresh để tải."));

        loadingIndicator.visibleProperty().bind(loading);
        btnRefresh.disableProperty().bind(loading);
        btnAdd.disableProperty().bind(loading.or(Bindings.createBooleanBinding(
                () -> !UserSession.getInstance().isLoggedIn())));
        btnEdit.disableProperty().bind(loading.or(tblItems.getSelectionModel().selectedItemProperty().isNull()));
        btnDelete.disableProperty().bind(loading.or(tblItems.getSelectionModel().selectedItemProperty().isNull()));
        tblItems.disableProperty().bind(loading);

        EventBus.getInstance().subscribe(EventType.CONNECTION_LOST, connectionLostListener);
        loadMyItemsAsync();
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
    public void handleRefresh() {
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
        confirm.setTitle("Xác nhận xoá");
        confirm.setHeaderText("Xoá sản phẩm: " + safe(selected.getName()));
        confirm.setContentText("Bạn chắc chắn muốn xoá sản phẩm này?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        runMutation(() -> sellerService.deleteItem(selected), "Đã xoá sản phẩm.");
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
            // Refresh cũng chỉ cần đăng nhập; quyền sở hữu item được kiểm tra ở server.
            AccessGuard.requireLogin();
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

    private Optional<SellerItemDto> showItemFormDialog(SellerItemDto existing) {
        Dialog<SellerItemDto> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm sản phẩm" : "Sửa sản phẩm");
        dialog.setHeaderText(existing == null ? "Nhập thông tin sản phẩm" : "Cập nhật thông tin sản phẩm");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfName = new TextField(existing == null ? "" : safe(existing.getName()));
        TextArea taDesc = new TextArea(existing == null ? "" : safe(existing.getDescription()));
        taDesc.setPrefRowCount(3);
        LocalDateTime defaultStartTime = LocalDateTime.now().withNano(0);
        LocalDateTime defaultEndTime = defaultStartTime.plusDays(1);

        /*
         * Category is required by the server because auction_items.category_id is a
         * foreign key. The schema seeds category id 1, so defaulting to 1 makes the
         * seller form usable immediately in a fresh database.
         */
        TextField tfCategoryId = new TextField(existing == null || existing.getCategoryId() <= 0 ? "1" : String.valueOf(existing.getCategoryId()));
        TextField tfStartPrice = new TextField(existing == null ? "" : MoneyFormat.plain(existing.getStartPrice()));
        TextField tfStartTime = new TextField(existing == null || existing.getStartTime() == null ? defaultStartTime.toString() : existing.getStartTime().toString());
        TextField tfEndTime = new TextField(existing == null || existing.getEndTime() == null ? defaultEndTime.toString() : existing.getEndTime().toString());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Tên"), tfName);
        grid.addRow(1, new Label("Mô tả"), taDesc);
        grid.addRow(2, new Label("Category ID (1=Phone, 2=Laptop, 3=Watch, 4=Motorbike)"), tfCategoryId);
        grid.addRow(3, new Label("Giá khởi điểm"), tfStartPrice);
        grid.addRow(4, new Label("StartTime (ISO)"), tfStartTime);
        grid.addRow(5, new Label("EndTime (ISO)"), tfEndTime);
        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateSellerForm(tfName, taDesc, tfCategoryId, tfStartPrice, tfStartTime, tfEndTime)) {
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
                    endTime
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
            try {
                dto.setCategoryId(parseCategoryId(tfCategoryId.getText()));
            } catch (IllegalArgumentException ex) {
                AlertHelper.showError("Sai dữ liệu", ex.getMessage());
                return null;
            }
            dto.setStartPrice(MoneyFormat.parse(tfStartPrice.getText()));
            dto.setStartTime(startTime);
            dto.setEndTime(endTime);
            return dto;
        });

        return dialog.showAndWait();
    }

    private boolean validateSellerForm(TextField tfName,
                                       TextArea taDesc,
                                       TextField tfCategoryId,
                                       TextField tfStartPrice,
                                       TextField tfStartTime,
                                       TextField tfEndTime) {
        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            parseCategoryId(tfCategoryId.getText());
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
                endTime
        );
        if (!result.valid()) {
            AlertHelper.showError("Dữ liệu chưa hợp lệ", firstError(result));
            return false;
        }
        return true;
    }

    private LocalDateTime parseDateTimeOrNull(String raw, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " phải theo dạng ISO, ví dụ 2026-05-26T20:00:00");
        }
    }

    private long parseCategoryId(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0L;
        }
        try {
            long categoryId = Long.parseLong(raw.trim());
            if (categoryId < 0) {
                throw new IllegalArgumentException("Category ID không được âm");
            }
            return categoryId;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Category ID phải là số nguyên");
        }
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
        if (loadingIndicator != null) {
            loadingIndicator.visibleProperty().unbind();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }
}
