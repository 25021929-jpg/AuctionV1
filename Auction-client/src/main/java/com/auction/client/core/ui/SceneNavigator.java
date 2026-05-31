package com.auction.client.core.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SceneNavigator {

  // ── Singleton Stage chính ──────────────────────────────────────────
  private static Stage mainStage;
  private static Object currentController;
  private static final Map<String, Scene> sceneCache = new HashMap<>();

  // Chỉ gọi đúng 1 lần khi app khởi động
  // Chỉ cần 1 stage thôi, có thể sử dụng được nhiều scene ->SingleTon
  public static void setStage(Stage stage) {
    if (mainStage != null) throw new IllegalStateException("mainStage đã được khởi tạo");
    mainStage = stage;
  }

  // Chuyển màn hình chính — tái sử dụng mainStage, cache scene
  public static void switchScene(String fxmlPath) {
    switchScene(fxmlPath, null);
  }

  /**
   * Switch scene và (tuỳ chọn) init controller ngay sau khi load.
   *
   * <p>Dùng để truyền param (auctionId, v.v.) mà không cần shared static state.
   */
  public static void switchScene(String fxmlPath, Consumer<Object> controllerInitializer) {
    try {
      // 1. Tải giao diện mới (hoặc lấy từ cache)
      Parent root;
      // Lưu ý: Tôi khuyên bạn nên load mới để tránh lỗi Controller cũ
      // trừ khi bạn thực sự muốn cache trạng thái nhập liệu.
      FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
      root = loader.load();

      Object controller = loader.getController();
      if (controllerInitializer != null) {
        controllerInitializer.accept(controller);
      }

      disposeCurrentController();

      // 2. Kiểm tra xem Stage đã có Scene nào chưa
      if (mainStage.getScene() == null) {
        // Lần đầu tiên: Tạo Scene mới
        Scene scene = new Scene(root);
        mainStage.setScene(scene);
      } else {
        // TỪ LẦN THỨ 2: KHÔNG đổi Scene, chỉ đổi cái "ruột" (Root)
        // Cách này giữ nguyên 100% trạng thái Maximized, FullScreen, Width, Height
        mainStage.getScene().setRoot(root);
      }

      // 3. Hiển thị stage nếu chưa hiển thị
      if (!mainStage.isShowing()) {
        mainStage.centerOnScreen();
        mainStage.show();
      }

      currentController = controller;

    } catch (IOException e) {
      AlertHelper.showError("Lỗi hệ thống", "Không thể tải: " + fxmlPath + "\n" + e.getMessage());
    }
  }

  private static void disposeCurrentController() {
    if (currentController instanceof DisposableController disposable) {
      try {
        disposable.dispose();
      } catch (Exception ignored) {
        // Không để lỗi cleanup làm hỏng navigation.
      }
    }
    currentController = null;
  }

  public static void clearCache(String fxmlPath) {
    sceneCache.remove(fxmlPath);
  }

  public static void clearCache() {
    sceneCache.clear();
  }

  // ── Stage phụ — tạo mới mỗi lần, không cache ──────────────────────

  // Modal — block mainStage, chờ user đóng mới dùng tiếp được
  // Ví dụ: xác nhận đặt giá, nhập OTP, cảnh báo
  public static void openModal(String fxmlPath, String title) {
    openModal(fxmlPath, title, null);
  }

  public static void openModal(String fxmlPath, String title, Runnable onClosed) {
    try {
      FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
      Stage modal = new Stage();
      modal.initModality(Modality.WINDOW_MODAL);
      modal.initOwner(mainStage); // gắn với mainStage
      modal.setTitle(title);
      modal.setScene(new Scene(loader.load()));
      modal.setResizable(false);

      if (onClosed != null) modal.setOnHidden(e -> onClosed.run());

      modal.showAndWait(); // block cho đến khi đóng
    } catch (IOException e) {
      AlertHelper.showError("Lỗi hệ thống", "Không thể tải: " + fxmlPath + "\n" + e.getMessage());
    }
  }

  // Cửa sổ phụ độc lập — không block mainStage
  // Ví dụ: cửa sổ xem ảnh, preview sản phẩm, lịch sử đấu giá
  public static void openWindow(String fxmlPath, String title) {
    try {
      FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
      Stage window = new Stage();
      window.initOwner(mainStage); // vẫn gắn owner để minimize cùng nhau
      window.setTitle(title);
      window.setScene(new Scene(loader.load()));
      window.show(); // không block
    } catch (IOException e) {
      AlertHelper.showError("Lỗi hệ thống", "Không thể tải: " + fxmlPath + "\n" + e.getMessage());
    }
  }
}
