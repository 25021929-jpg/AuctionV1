package com.auction.client.feature.wallet.ui;

import com.auction.client.core.error.ErrorHandler;
import com.auction.client.core.session.UserSession;
import com.auction.client.core.ui.AlertHelper;
import com.auction.client.feature.wallet.service.WalletService;
import com.auction.client.feature.wallet.service.WalletServiceImpl;
import com.auction.shared.dto.wallet.WalletSummaryDto;
import com.auction.shared.dto.wallet.WalletTransactionDto;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/** Dialog dùng chung để nạp tiền và xem lịch sử giao dịch ví. */
public final class WalletDialog {
  private static final WalletService walletService = new WalletServiceImpl();
  private static final DateTimeFormatter TIME_FMT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  private WalletDialog() {}

  public static void showWallet() {
    try {
      WalletSummaryDto summary = walletService.getSummary();
      List<WalletTransactionDto> history = walletService.getTransactions(50);

      Dialog<Void> dialog = new Dialog<>();
      dialog.setTitle("Ví tài khoản");
      dialog.setHeaderText(
          "Số dư hiện tại: "
              + money(
                  summary == null ? UserSession.getInstance().getBalance() : summary.getBalance()));
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

      ButtonType depositType = new ButtonType("Nạp tiền", ButtonBar.ButtonData.OTHER);
      dialog.getDialogPane().getButtonTypes().add(0, depositType);

      TableView<WalletTransactionDto> table = createHistoryTable(history);
      Label hint =
          new Label("Tiền nạp phải là số nguyên dương. Không chấp nhận 0, số âm hoặc tiền lẻ.");
      hint.setWrapText(true);
      VBox content = new VBox(10, hint, table);
      content.setPrefSize(720, 420);
      dialog.getDialogPane().setContent(content);

      Button depositButton = (Button) dialog.getDialogPane().lookupButton(depositType);
      depositButton.addEventFilter(
          javafx.event.ActionEvent.ACTION,
          event -> {
            event.consume();
            if (showDepositDialog()) {
              dialog.close();
              showWallet();
            }
          });

      dialog.showAndWait();
    } catch (Exception ex) {
      AlertHelper.showError("Lỗi ví tài khoản", ErrorHandler.getUserMessage(ex));
    }
  }

  public static boolean showDepositDialog() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Nạp tiền");
    dialog.setHeaderText("Nhập số tiền muốn nạp");
    dialog.setContentText("Số tiền:");

    Optional<String> raw = dialog.showAndWait();
    if (raw.isEmpty()) {
      return false;
    }

    BigDecimal amount;
    try {
      amount = new BigDecimal(raw.get().trim());
    } catch (Exception ex) {
      AlertHelper.showError("Số tiền không hợp lệ", "Số tiền nạp phải là số nguyên dương.");
      return false;
    }

    if (amount.signum() <= 0) {
      AlertHelper.showError("Số tiền không hợp lệ", "Không được nạp 0 đồng hoặc số tiền âm.");
      return false;
    }
    if (amount.stripTrailingZeros().scale() > 0) {
      AlertHelper.showError(
          "Số tiền không hợp lệ", "Số tiền nạp phải là số nguyên, không nhập tiền lẻ.");
      return false;
    }

    try {
      WalletSummaryDto summary = walletService.deposit(amount);
      AlertHelper.showInfo("Nạp tiền thành công", "Số dư mới: " + money(summary.getBalance()));
      return true;
    } catch (Exception ex) {
      AlertHelper.showError("Không thể nạp tiền", ErrorHandler.getUserMessage(ex));
      return false;
    }
  }

  private static TableView<WalletTransactionDto> createHistoryTable(
      List<WalletTransactionDto> rows) {
    TableView<WalletTransactionDto> table = new TableView<>();
    table.setPrefHeight(360);
    table.setPlaceholder(new Label("Chưa có giao dịch nào."));

    TableColumn<WalletTransactionDto, String> timeCol = new TableColumn<>("Thời gian");
    timeCol.setPrefWidth(160);
    timeCol.setCellValueFactory(
        cell ->
            new ReadOnlyStringWrapper(
                cell.getValue().getCreatedAt() == null
                    ? "-"
                    : TIME_FMT.format(cell.getValue().getCreatedAt())));

    TableColumn<WalletTransactionDto, String> typeCol = new TableColumn<>("Loại");
    typeCol.setPrefWidth(140);
    typeCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(typeText(cell.getValue().getType())));

    TableColumn<WalletTransactionDto, String> amountCol = new TableColumn<>("Số tiền");
    amountCol.setPrefWidth(130);
    amountCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(money(cell.getValue().getAmount())));

    TableColumn<WalletTransactionDto, String> balanceCol = new TableColumn<>("Số dư sau GD");
    balanceCol.setPrefWidth(130);
    balanceCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(money(cell.getValue().getBalanceAfter())));

    TableColumn<WalletTransactionDto, String> descCol = new TableColumn<>("Nội dung");
    descCol.setPrefWidth(230);
    descCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().getDescription()));

    table.getColumns().setAll(timeCol, typeCol, amountCol, balanceCol, descCol);
    table.setItems(FXCollections.observableArrayList(rows == null ? List.of() : rows));
    return table;
  }

  private static String typeText(String type) {
    if ("DEPOSIT".equals(type)) return "Nạp tiền";
    if ("AUCTION_PAYMENT".equals(type)) return "Thanh toán đấu giá";
    if ("AUCTION_RECEIVE".equals(type)) return "Nhận tiền bán";
    return type == null ? "-" : type;
  }

  private static String money(BigDecimal value) {
    return value == null ? "0" : value.stripTrailingZeros().toPlainString();
  }
}
