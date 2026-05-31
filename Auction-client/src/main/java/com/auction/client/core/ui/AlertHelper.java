package com.auction.client.core.ui;

import com.auction.client.core.error.ErrorHandler;
import javafx.scene.control.Alert;

/** Helper hiển thị Alert trên JavaFX. */
public class AlertHelper {

  public static void showInfo(String title, String content) {
    showAlert(Alert.AlertType.INFORMATION, title, content);
  }

  public static void showError(String title, String content) {
    showAlert(Alert.AlertType.ERROR, title, content);
  }

  /** Hiển thị lỗi theo ErrorHandler chung để thông báo nhất quán giữa các màn. */
  public static void showException(String title, Throwable throwable) {
    showError(title, ErrorHandler.getUserMessage(throwable));
  }

  private static void showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
