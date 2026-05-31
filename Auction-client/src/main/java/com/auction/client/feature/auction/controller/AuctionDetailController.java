package com.auction.client.feature.auction.controller;

import com.auction.client.core.event.AppEvent;
import com.auction.client.core.event.EventBus;
import com.auction.client.core.event.EventListener;
import com.auction.client.core.event.EventType;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.client.core.ui.DisposableController;
import com.auction.client.core.ui.FxAsync;
import com.auction.client.core.ui.AlertHelper;
import com.auction.shared.dto.auction.AuctionDetailDto;
import com.auction.shared.domain.AuctionStatus;
import com.auction.client.feature.auction.service.AuctionService;
import com.auction.client.feature.auction.service.AuctionServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import com.auction.client.core.util.MoneyFormat;

/**
 * Màn hình chi tiết sản phẩm/phiên đấu giá.
 *
 * View gọi {@link AuctionService} để tải dữ liệu chi tiết từ server.
 * Nếu server chưa hỗ trợ đầy đủ field, controller sẽ hiển thị theo dữ liệu nhận được.
 */
public class AuctionDetailController implements DisposableController {

    @FXML private Label lblItemName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblStartPrice;
    @FXML private Label lblStartTime;
    @FXML private Label lblEndTime;
    @FXML private Label lblStatus;
    @FXML private Label lblDescription;
    @FXML private Label lblCategory;
    @FXML private Label lblSeller;
    @FXML private Label lblLeader;
    @FXML private Label lblTotalBids;

    @FXML private Button btnRefresh;
    @FXML private Button btnGoLive;
    @FXML private ProgressIndicator loadingIndicator;

    /** auctionId hiện tại (được set từ màn danh sách). */
    private Long auctionId;

    private boolean initialized = false;
    private final AuctionService auctionService = new AuctionServiceImpl();

    private final EventListener connectionLostListener = this::onConnectionLost;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
        if (initialized) {
            loadDetail();
        }
    }

    @FXML
    public void initialize() {
        initialized = true;

        EventBus.getInstance().subscribe(EventType.CONNECTION_LOST, connectionLostListener);

        lblItemName.setText("(Đang tải...)");
        lblCurrentPrice.setText("-");
        if (lblStartPrice != null) lblStartPrice.setText("-");
        if (lblStartTime != null) lblStartTime.setText("-");
        lblEndTime.setText("-");
        lblStatus.setText("-");
        if (lblDescription != null) lblDescription.setText("-");
        if (lblCategory != null) lblCategory.setText("-");
        if (lblSeller != null) lblSeller.setText("-");
        if (lblLeader != null) lblLeader.setText("-");
        if (lblTotalBids != null) lblTotalBids.setText("0");

        // Nếu auctionId đã được set trước initialize (trường hợp hiếm), vẫn load.
        if (auctionId != null) {
            loadDetail();
        }
    }

    private void loadDetail() {
        if (auctionId == null) return;

        setLoading(true);
        FxAsync.run(
                () -> {
                    try {
                        return auctionService.fetchAuctionDetail(auctionId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                dto -> {
                    if (dto == null) {
                        lblItemName.setText("(Không có dữ liệu)");
                        return;
                    }

                    lblItemName.setText(dto.getItemName() != null ? dto.getItemName() : "(Không có tên)");
                    lblCurrentPrice.setText(MoneyFormat.grouped(dto.getCurrentPrice()));
                    if (lblStartPrice != null) lblStartPrice.setText(MoneyFormat.grouped(dto.getStartingPrice()));
                    if (lblDescription != null) lblDescription.setText(blankAsDash(dto.getDescription()));
                    if (lblCategory != null) lblCategory.setText(blankAsDash(dto.getCategoryName()));
                    if (lblSeller != null) lblSeller.setText(blankAsDash(dto.getSellerName()));
                    if (lblLeader != null) lblLeader.setText(blankAsDash(dto.getLeaderUsername()));
                    if (lblTotalBids != null) lblTotalBids.setText(String.valueOf(dto.getTotalBids()));
                    if (lblStartTime != null) {
                        lblStartTime.setText(dto.getStartTime() == null ? "-" : DT_FMT.format(dto.getStartTime()));
                    }
                    AuctionStatus status = dto.getStatus();
                    lblStatus.setText(status != null ? status.name() : "-");
                    if (btnGoLive != null) {
                        btnGoLive.setDisable(status != null && !status.isBiddable());
                    }
                    if (dto.getEndTime() != null) {
                        lblEndTime.setText(DT_FMT.format(dto.getEndTime()));
                    } else {
                        lblEndTime.setText("-");
                    }
                },
                err -> {
                    lblItemName.setText("(Lỗi tải dữ liệu)");
                    lblStatus.setText("-");
                    AlertHelper.showException("Không thể tải chi tiết", err);
                },
                () -> setLoading(false)
        );
    }

    @FXML
    public void handleRefresh() {
        loadDetail();
    }

    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
        if (btnRefresh != null) {
            btnRefresh.setDisable(loading);
        }
        if (btnGoLive != null) {
            if (loading) {
                btnGoLive.setDisable(true);
            }
        }
    }

    @FXML
    public void handleGoToLiveBidding() {
        SceneNavigator.switchScene(
                ScenePaths.LIVE_BIDDING,
                controller -> {
                    // Truyền auctionId sang màn live bidding.
                    if (controller instanceof com.auction.client.feature.bidding.controller.LiveBiddingController c) {
                        c.setAuctionId(auctionId);
                    }
                }
        );
    }

    @FXML
    public void handleBack() {
        dispose();
        SceneNavigator.switchScene(ScenePaths.AUCTION_LIST);
    }

    @Override
    public void dispose() {
        EventBus.getInstance().unsubscribe(EventType.CONNECTION_LOST, connectionLostListener);
    }

    private String blankAsDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void onConnectionLost(AppEvent event) {
        // MainClient đã hiển thị alert; controller chỉ khóa thao tác để tránh lỗi tiếp theo.
        setLoading(true);
        if (btnGoLive != null) btnGoLive.setDisable(true);
    }
}
