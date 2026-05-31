package com.auction.client.core.ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Toast {

  public enum Type {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
  }

  // Hàm chính — gọi từ bất kỳ controller nào
  public static void show(StackPane root, String message) {
    show(root, message, Type.SUCCESS, 2, null);
  }

  public static void show(StackPane root, String message, Type type) {
    show(root, message, type, 2, null);
  }

  public static void show(
      StackPane root, String message, Type type, double durationSeconds, Runnable onFinished) {
    Label toast = new Label(message);
    toast.setStyle(buildStyle(type));

    // 2. Đặt ở góc trên bên trái
    StackPane.setAlignment(toast, Pos.TOP_LEFT);

    // 3. Căn lề: Cách bên trái 20px, cách trên đỉnh 20px
    StackPane.setMargin(toast, new Insets(20, 0, 0, 20));

    root.getChildren().add(toast);

    // logic animation
    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(0.85); // Chỉ hiện lên đến 85% để giữ độ trong suốt

    PauseTransition hold = new PauseTransition(Duration.seconds(durationSeconds));

    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
    fadeOut.setFromValue(0.85);
    fadeOut.setToValue(0);
    fadeOut.setOnFinished(
        e -> {
          root.getChildren().remove(toast);
          if (onFinished != null) onFinished.run();
        });

    new SequentialTransition(fadeIn, hold, fadeOut).play();
  }

  /*
  Để nhìn xuyên thấu, chúng ta không dùng mã màu Hex (#...) bình thường mà dùng hàm rgba (Red, Green, Blue, Alpha).
   Số cuối cùng (Alpha) càng nhỏ thì càng nhìn xuyên thấu rõ.
   */
  private static String buildStyle(Type type) {
    // Sử dụng RGBA: 0.7 hoặc 0.8 là độ trong suốt vừa đẹp
    String rgba =
        switch (type) {
          case SUCCESS -> "rgba(46, 125, 50, 0.8)";
          case ERROR -> "rgba(198, 40, 40, 0.8)";
          case WARNING -> "rgba(230, 81, 0, 0.8)";
          case INFO -> "rgba(21, 101, 192, 0.8)";
        };

    return """
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-padding: 10 20 10 20;
            -fx-background-radius: 8;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            """
        .formatted(rgba);
  }
}
