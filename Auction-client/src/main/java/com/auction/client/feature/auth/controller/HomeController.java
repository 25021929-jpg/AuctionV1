package com.auction.client.feature.auth.controller;

import com.auction.client.core.security.AccessGuard;
import com.auction.client.core.session.ClientSessionManager;
import com.auction.client.core.session.UserSession;
import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.shared.domain.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/** Home theo role: chỉ hiện chức năng phù hợp với người đang đăng nhập. */
public class HomeController {

    @FXML private Label lblWelcome;
    @FXML private Button btnSeller;
    @FXML private Button btnAdmin;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            lblWelcome.setText("Bạn chưa đăng nhập.");
            setVisibleAndManaged(btnSeller, false);
            setVisibleAndManaged(btnAdmin, false);
            return;
        }

        UserRole role = session.getRole();
        lblWelcome.setText("Xin chào, " + session.displayName() + " (" + roleText(role) + ")");
        setVisibleAndManaged(btnSeller, role == UserRole.SELLER || role == UserRole.ADMIN);
        setVisibleAndManaged(btnAdmin, role == UserRole.ADMIN);
    }

    @FXML
    public void handleOpenAuctionList() {
        SceneNavigator.switchScene(ScenePaths.AUCTION_LIST);
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

    private void setVisibleAndManaged(Button button, boolean value) {
        if (button != null) {
            button.setVisible(value);
            button.setManaged(value);
        }
    }

    private String roleText(UserRole role) {
        return role == null ? "UNKNOWN" : role.name();
    }
}
