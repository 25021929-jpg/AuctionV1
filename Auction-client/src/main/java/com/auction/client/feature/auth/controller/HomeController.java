package com.auction.client.feature.auth.controller;

import com.auction.client.core.security.AccessGuard;
import com.auction.client.core.session.ClientSessionManager;
import com.auction.client.core.session.UserSession;
import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.FxAsync;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.client.core.util.MoneyFormat;
import com.auction.client.feature.auction.controller.AuctionDetailController;
import com.auction.client.feature.auction.service.AuctionService;
import com.auction.client.feature.auction.service.AuctionServiceImpl;
import com.auction.shared.domain.UserRole;
import com.auction.shared.dto.auction.AuctionSummaryDto;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * Home chính sau đăng nhập.
 *
 * Giao diện có 3 tab bên trái:
 * - Trang chủ: danh sách sản phẩm đang đấu giá.
 * - Trang cá nhân: thông tin tài khoản hiện tại.
 * - Lịch sử giao dịch: khung UI sẵn sàng nối API sau.
 */
public class HomeController {

    private static final String ACTIVE_NAV_CLASS = "side-nav-button-active";
    private static final String NOT_UPDATED = "Chưa cập nhật";

    @FXML private Label lblWelcome;
    @FXML private Label lblRole;

    @FXML private Button btnNavHome;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavHistory;
    @FXML private Button btnSeller;
    @FXML private Button btnAdmin;
    @FXML private Button btnRefreshAuctions;

    @FXML private VBox homePane;
    @FXML private VBox profilePane;
    @FXML private VBox historyPane;

    @FXML private TableView<AuctionSummaryDto> auctionTable;
    @FXML private TableColumn<AuctionSummaryDto, Long> colHomeAuctionId;
    @FXML private TableColumn<AuctionSummaryDto, String> colHomeItemName;
    @FXML private TableColumn<AuctionSummaryDto, BigDecimal> colHomePrice;
    @FXML private TableColumn<AuctionSummaryDto, String> colHomeStatus;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private Label lblProfileName;
    @FXML private Label lblProfileRole;
    @FXML private Label lblProfileUsername;
    @FXML private Label lblProfileEmail;
    @FXML private Label lblProfilePhone;
    @FXML private Label lblProfileDob;
    @FXML private Label lblProfileBalance;

    @FXML private TableView<TransactionHistoryRow> historyTable;
    @FXML private TableColumn<TransactionHistoryRow, String> colHistoryTime;
    @FXML private TableColumn<TransactionHistoryRow, String> colHistoryType;
    @FXML private TableColumn<TransactionHistoryRow, String> colHistoryAmount;
    @FXML private TableColumn<TransactionHistoryRow, String> colHistoryNote;

    private final AuctionService auctionService = new AuctionServiceImpl();

    @FXML
    public void initialize() {
        setupSessionInfo();
        setupAuctionTable();
        setupHistoryTable();

        showTab(homePane, btnNavHome);
        loadAuctionsAsync();
    }

    private void setupSessionInfo() {
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            if (lblWelcome != null) lblWelcome.setText("Bạn chưa đăng nhập.");
            if (lblRole != null) lblRole.setText("Khách");
            setVisibleAndManaged(btnSeller, false);
            setVisibleAndManaged(btnAdmin, false);
            return;
        }

        UserRole role = session.getRole();
        Object user = session.getCurrentUser();
        String roleText = roleText(role);

        if (lblWelcome != null) lblWelcome.setText("Xin chào, " + session.displayName());
        if (lblRole != null) lblRole.setText("Vai trò: " + roleText);

        if (lblProfileName != null) lblProfileName.setText(session.displayName());
        if (lblProfileRole != null) lblProfileRole.setText("Vai trò: " + roleText);
        if (lblProfileUsername != null) lblProfileUsername.setText(readText(user, "getUsername"));
        if (lblProfileEmail != null) lblProfileEmail.setText(readText(user, "getEmail"));
        if (lblProfilePhone != null) lblProfilePhone.setText(readText(user, "getPhoneNumber", "getPhone"));
        if (lblProfileDob != null) lblProfileDob.setText(readText(user, "getDateOfBirth", "getBirthDate", "getDob"));
        if (lblProfileBalance != null) lblProfileBalance.setText(readMoney(user, "getBalance", "getWalletBalance", "getAccountBalance"));

        // Seller/Admin vẫn có lối vào riêng nhưng không làm rối thanh menu chính 3 mục.
        setVisibleAndManaged(btnSeller, role == UserRole.SELLER || role == UserRole.ADMIN);
        setVisibleAndManaged(btnAdmin, role == UserRole.ADMIN);
    }

    private void setupAuctionTable() {
        if (auctionTable == null) return;

        colHomeAuctionId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colHomeItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colHomePrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colHomeStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));

        colHomePrice.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? "" : MoneyFormat.grouped(value));
            }
        });

        auctionTable.setPlaceholder(new Label("Chưa có sản phẩm đang đấu giá hoặc server chưa trả dữ liệu."));
        auctionTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                AuctionSummaryDto selected = auctionTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SceneNavigator.switchScene(
                            ScenePaths.AUCTION_DETAIL,
                            controller -> {
                                if (controller instanceof AuctionDetailController c) {
                                    c.setAuctionId(selected.getAuctionId());
                                }
                            }
                    );
                }
            }
        });
    }

    private void setupHistoryTable() {
        if (historyTable == null) return;

        colHistoryTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colHistoryType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colHistoryAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colHistoryNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Hiện tại client chưa có API lịch sử giao dịch, nên để bảng trống có placeholder rõ ràng.
        historyTable.setItems(FXCollections.observableArrayList());
        historyTable.setPlaceholder(new Label("Chưa có lịch sử giao dịch. Khi server có API, chỉ cần đổ dữ liệu vào bảng này."));
    }

    @FXML
    public void handleShowHome() {
        showTab(homePane, btnNavHome);
    }

    @FXML
    public void handleShowProfile() {
        showTab(profilePane, btnNavProfile);
    }

    @FXML
    public void handleShowHistory() {
        showTab(historyPane, btnNavHistory);
    }

    @FXML
    public void handleRefreshAuctions() {
        loadAuctionsAsync();
    }

    @FXML
    public void handleOpenSeller() {
        try {
            AccessGuard.requireAnyRole(UserRole.SELLER, UserRole.ADMIN);
            SceneNavigator.switchScene(ScenePaths.SELLER_DASHBOARD);
        } catch (Exception ex) {
            AlertHelper.showException("Không có quyền", ex);
        }
    }

    @FXML
    public void handleOpenAdmin() {
        try {
            AccessGuard.requireRole(UserRole.ADMIN);
            SceneNavigator.switchScene(ScenePaths.ADMIN_DASHBOARD);
        } catch (Exception ex) {
            AlertHelper.showException("Không có quyền", ex);
        }
    }

    @FXML
    public void handleLogout() {
        ClientSessionManager.logoutToLogin();
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
                auctions -> {
                    if (auctionTable != null) {
                        auctionTable.setItems(FXCollections.observableArrayList(auctions));
                    }
                },
                err -> AlertHelper.showException("Không thể tải danh sách đấu giá", err),
                () -> setLoading(false)
        );
    }

    private void setLoading(boolean loading) {
        if (loadingIndicator != null) loadingIndicator.setVisible(loading);
        if (btnRefreshAuctions != null) btnRefreshAuctions.setDisable(loading);
        if (auctionTable != null) auctionTable.setDisable(loading);
    }

    private void showTab(VBox selectedPane, Button selectedButton) {
        setVisibleAndManaged(homePane, selectedPane == homePane);
        setVisibleAndManaged(profilePane, selectedPane == profilePane);
        setVisibleAndManaged(historyPane, selectedPane == historyPane);

        setActiveButton(btnNavHome, selectedButton == btnNavHome);
        setActiveButton(btnNavProfile, selectedButton == btnNavProfile);
        setActiveButton(btnNavHistory, selectedButton == btnNavHistory);
    }

    private void setActiveButton(Button button, boolean active) {
        if (button == null) return;
        if (active) {
            if (!button.getStyleClass().contains(ACTIVE_NAV_CLASS)) {
                button.getStyleClass().add(ACTIVE_NAV_CLASS);
            }
        } else {
            button.getStyleClass().remove(ACTIVE_NAV_CLASS);
        }
    }

    private void setVisibleAndManaged(Button button, boolean value) {
        if (button != null) {
            button.setVisible(value);
            button.setManaged(value);
        }
    }

    private void setVisibleAndManaged(VBox pane, boolean value) {
        if (pane != null) {
            pane.setVisible(value);
            pane.setManaged(value);
        }
    }

    private String roleText(UserRole role) {
        return role == null ? "UNKNOWN" : role.name();
    }

    private String readText(Object target, String... getterNames) {
        Object value = readValue(target, getterNames);
        if (value == null) return NOT_UPDATED;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? NOT_UPDATED : text;
    }

    private String readMoney(Object target, String... getterNames) {
        Object value = readValue(target, getterNames);
        if (value == null) return NOT_UPDATED;

        if (value instanceof BigDecimal bd) {
            return MoneyFormat.grouped(bd);
        }
        if (value instanceof Number number) {
            return MoneyFormat.grouped(BigDecimal.valueOf(number.doubleValue()));
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? NOT_UPDATED : text;
    }

    private Object readValue(Object target, String... getterNames) {
        if (target == null || getterNames == null) return null;

        for (String getterName : getterNames) {
            try {
                Method method = target.getClass().getMethod(getterName);
                return method.invoke(target);
            } catch (Exception ignored) {
                // DTO có thể chưa thống nhất tên getter giữa các phiên bản server/shared.
                // Bỏ qua getter không tồn tại để giao diện không bị crash.
            }
        }
        return null;
    }

    /** Row mẫu cho bảng lịch sử giao dịch. Khi có API thật, map response vào class này là đủ. */
    public static final class TransactionHistoryRow {
        private final String time;
        private final String type;
        private final String amount;
        private final String note;

        public TransactionHistoryRow(String time, String type, String amount, String note) {
            this.time = time;
            this.type = type;
            this.amount = amount;
            this.note = note;
        }

        public String getTime() {
            return time;
        }

        public String getType() {
            return type;
        }

        public String getAmount() {
            return amount;
        }

        public String getNote() {
            return note;
        }
    }
}
