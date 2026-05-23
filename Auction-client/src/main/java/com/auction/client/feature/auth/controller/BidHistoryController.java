package com.auction.client.feature.auth.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.Node;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Bid History Visualization.
 *
 * Cách dùng từ AuctionController (hoặc nơi nào mở màn hình này):
 *   FXMLLoader loader = new FXMLLoader(getClass().getResource(
 *       "/com/auction/client/feature/auction/view/bid-history-view.fxml"));
 *   Parent root = loader.load();
 *   BidHistoryController ctrl = loader.getController();
 *   ctrl.initAuction(auctionId, auctionName, startPrice);
 */
public class BidHistoryController implements Initializable {

    // ── FXML nodes ──────────────────────────────────────────────
    @FXML private LineChart<String, Number> bidLineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis   yAxis;

    @FXML private Label auctionNameLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label totalBidsLabel;
    @FXML private Label leaderLabel;
    @FXML private Label bidCountBadge;
    @FXML private Label statusLabel;
    @FXML private Label liveIndicator;

    @FXML private TableView<BidRow>    bidHistoryTable;
    @FXML private TableColumn<BidRow, String> colBidder;
    @FXML private TableColumn<BidRow, String> colAmount;
    @FXML private TableColumn<BidRow, String> colTime;

    // ── State ────────────────────────────────────────────────────
    private XYChart.Series<String, Number> series;
    private final ObservableList<BidRow>   tableData = FXCollections.observableArrayList();
    private int    bidIndex    = 0;   // thứ tự bid (dùng cho trục X)
    private double maxBidValue = 0;
    private String leadBidder  = "—";
    private double startPrice  = 0;
    private String currentUsername = ""; // set từ bên ngoài nếu cần tô màu bid của mình

    private static final NumberFormat VND = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ── Initializable ────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupChart();
        setupTable();
    }

    // ── Setup ────────────────────────────────────────────────────

    private void setupChart() {
        series = new XYChart.Series<>();
        series.setName("Giá bid");
        bidLineChart.getData().add(series);

        // Ẩn legend vì chỉ có 1 series
        bidLineChart.setLegendVisible(false);
        bidLineChart.setTitle(null);
        bidLineChart.setCreateSymbols(true);
        bidLineChart.setAnimated(true);
    }

    private void setupTable() {
        colBidder.setCellValueFactory(c -> c.getValue().bidderProperty());
        colAmount.setCellValueFactory(c -> c.getValue().amountProperty());
        colTime.setCellValueFactory(c -> c.getValue().timeProperty());

        // Tô màu dòng bid cao nhất
        bidHistoryTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(BidRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.isMaxBid()) {
                    setStyle("-fx-background-color: rgba(0,229,160,0.08);");
                } else if (item.isMyBid()) {
                    setStyle("-fx-background-color: rgba(200,168,75,0.08);");
                } else {
                    setStyle("");
                }
            }
        });

        bidHistoryTable.setItems(tableData);
    }

    // ── Public API (gọi từ bên ngoài sau khi load FXML) ──────────

    /** Khởi tạo với thông tin phiên đấu giá */
    public void initAuction(String auctionId, String auctionName, double startPrice) {
        this.startPrice = startPrice;
        auctionNameLabel.setText(auctionName + "  (#" + auctionId + ")");
        startPriceLabel.setText(fmtVnd(startPrice) + "đ");
        currentPriceLabel.setText(fmtVnd(startPrice) + "đ");
        statusLabel.setText("Đang theo dõi phiên: " + auctionId);
    }

    /** Đặt username người dùng hiện tại (để tô màu bid của mình) */
    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    /**
     * PHƯƠNG THỨC CHÍNH — gọi mỗi khi nhận BidTransaction từ server.
     *
     * Bọc trong Platform.runLater() nếu gọi từ thread Socket:
     *   Platform.runLater(() -> controller.addBid(bidder, amount));
     */
    public void addBid(String bidder, double amount) {
        bidIndex++;
        String timeLabel = LocalTime.now().format(TIME_FMT);
        String xLabel    = "#" + bidIndex;

        // 1. Thêm điểm vào chart
        XYChart.Data<String, Number> point = new XYChart.Data<>(xLabel, amount);
        series.getData().add(point);

        // 2. Tô màu điểm sau khi node đã được render
        point.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) styleChartPoint(point, bidder, amount);
        });
        // Trường hợp node đã sẵn sàng ngay
        if (point.getNode() != null) styleChartPoint(point, bidder, amount);

        // 3. Refresh màu tất cả điểm (để reset màu cũ khi có điểm mới cao hơn)
        if (amount > maxBidValue) {
            maxBidValue = amount;
            leadBidder  = bidder;
            refreshAllPointColors();
        }

        // 4. Cập nhật bảng
        boolean isMax  = (amount == maxBidValue);
        boolean isMine = bidder.equals(currentUsername);
        tableData.add(0, new BidRow(bidder, fmtVnd(amount) + "đ", timeLabel, isMax, isMine));

        // 5. Cập nhật stat cards
        currentPriceLabel.setText(fmtVnd(amount) + "đ");
        leaderLabel.setText(leadBidder);
        totalBidsLabel.setText(String.valueOf(bidIndex));
        bidCountBadge.setText(String.valueOf(bidIndex));

        // 6. Giới hạn hiển thị 20 điểm gần nhất để chart không quá rối
        if (series.getData().size() > 20) {
            series.getData().remove(0);
        }
    }

    // ── Xử lý filter nút toggle ──────────────────────────────────

    @FXML private void handleFilterAll()    { applyFilter(Integer.MAX_VALUE); }
    @FXML private void handleFilterLast10() { applyFilter(10); }
    @FXML private void handleFilterLast5()  { applyFilter(5); }

    private void applyFilter(int maxPoints) {
        // Lấy tất cả data từ bảng, hiển thị maxPoints gần nhất lên chart
        series.getData().clear();
        int start = Math.max(0, tableData.size() - maxPoints);
        for (int i = tableData.size() - 1; i >= start; i--) {
            BidRow row = tableData.get(i);
            series.getData().add(new XYChart.Data<>(
                "#" + (tableData.size() - i), parseAmount(row.getAmount())));
        }
    }

    @FXML private void handleClose() {
        bidLineChart.getScene().getWindow().hide();
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void styleChartPoint(XYChart.Data<String, Number> point,
                                  String bidder, double amount) {
        Node node = point.getNode();
        if (node == null) return;

        Tooltip tip = new Tooltip(bidder + "\n" + fmtVnd(amount) + "đ");
        tip.setStyle("-fx-background-color: #051529; -fx-text-fill: #E8EEF7; "
                   + "-fx-border-color: rgba(21,101,255,0.4); -fx-border-width:1; "
                   + "-fx-border-radius:6; -fx-background-radius:6; "
                   + "-fx-font-size:12px; -fx-padding: 6 12;");
        Tooltip.install(node, tip);

        if (amount == maxBidValue) {
            node.getStyleClass().removeAll("bid-mine");
            node.getStyleClass().add("bid-max");
        } else if (bidder.equals(currentUsername)) {
            node.getStyleClass().add("bid-mine");
        }
    }

    /** Sau khi có maxBid mới, vẽ lại màu tất cả các điểm */
    private void refreshAllPointColors() {
        for (XYChart.Data<String, Number> d : series.getData()) {
            Node n = d.getNode();
            if (n == null) continue;
            n.getStyleClass().removeAll("bid-max", "bid-mine");
            if (d.getYValue().doubleValue() == maxBidValue) {
                n.getStyleClass().add("bid-max");
            }
        }
    }

    private String fmtVnd(double amount) {
        return VND.format((long) amount);
    }

    private double parseAmount(String amountStr) {
        try {
            return Double.parseDouble(amountStr.replace(",", "").replace("đ", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ── Inner model class ─────────────────────────────────────────

    public static class BidRow {
        private final javafx.beans.property.SimpleStringProperty bidder;
        private final javafx.beans.property.SimpleStringProperty amount;
        private final javafx.beans.property.SimpleStringProperty time;
        private final boolean maxBid;
        private final boolean myBid;

        public BidRow(String bidder, String amount, String time,
                      boolean maxBid, boolean myBid) {
            this.bidder = new javafx.beans.property.SimpleStringProperty(bidder);
            this.amount = new javafx.beans.property.SimpleStringProperty(amount);
            this.time   = new javafx.beans.property.SimpleStringProperty(time);
            this.maxBid = maxBid;
            this.myBid  = myBid;
        }

        public javafx.beans.property.StringProperty bidderProperty() { return bidder; }
        public javafx.beans.property.StringProperty amountProperty() { return amount; }
        public javafx.beans.property.StringProperty timeProperty()   { return time;   }
        public String getAmount() { return amount.get(); }
        public boolean isMaxBid() { return maxBid; }
        public boolean isMyBid()  { return myBid;  }
    }
}
