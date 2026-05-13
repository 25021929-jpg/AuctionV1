package com.auction.client.core.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class UIAnimations {

    public static void entrance(Node target) {
        // Bước 1 — Ẩn form trước
        target.setOpacity(0);
        target.setTranslateY(30); // ← bắt đầu từ dưới lên 30px

        // Bước 2 — FadeIn: opacity 0 → 1
        FadeTransition fade = new FadeTransition(
                Duration.millis(600), target
        );
        fade.setFromValue(0);
        fade.setToValue(1);

        // Bước 3 — Slide up: translateY 30 → 0
        TranslateTransition slide = new TranslateTransition(
                Duration.millis(600), target
        );
        slide.setFromY(30);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT); // ← giảm tốc ở cuối

        // Bước 4 — Chạy cả 2 cùng lúc
        ParallelTransition entrance = new ParallelTransition(fade, slide);
        entrance.setDelay(Duration.millis(100)); // ← delay nhỏ trước khi bắt đầu
        entrance.play();
    }

    public static void fadeOut(Node target, Runnable onFinish) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), target);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> onFinish.run()); // ← callback sau khi xong
        fade.play();
    }

    public static void shake(Node target) {
        // Hiệu ứng rung khi nhập sai
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), target);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}
