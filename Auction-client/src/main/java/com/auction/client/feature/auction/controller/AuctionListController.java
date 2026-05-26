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
import com.auction.shared.dto.auction.AuctionSummaryDto;
import com.auction.client.feature.auction.service.AuctionService;
import com.auction.client.feature.auction.service.AuctionServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.math.BigDecimal;
import com.auction.client.core.util.MoneyFormat;

public class AuctionListController implements DisposableController {

    @FXML private TableView<AuctionSummaryDto> auctionTable;
    @FXML private TableColumn<AuctionSummaryDto, Long> colId;
    @FXML private TableColumn<AuctionSummaryDto, String> colName;
    @FXML private TableColumn<AuctionSummaryDto, BigDecimal> colPrice;
    @FXML private TableColumn<AuctionSummaryDto, String> colStatus;

    @FXML private Button btnRefresh;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuctionService auctionService = new AuctionServiceImpl();

    private final EventListener connectionLostListener = this::onConnectionLost;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        colPrice.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
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
                () -> setLoading(false)
        );
    }

    @FXML
    public void handleBack() {
        dispose();
        SceneNavigator.switchScene(ScenePaths.HOME);
    }

    @Override
    public void dispose() {
        EventBus.getInstance().unsubscribe(EventType.CONNECTION_LOST, connectionLostListener);
    }

    private void onConnectionLost(AppEvent event) {
        // MainClient đã hiển thị alert; controller chỉ khóa thao tác để tránh lỗi tiếp theo.
        setLoading(true);
    }
}
