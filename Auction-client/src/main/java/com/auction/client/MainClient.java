package com.auction.client;

import com.auction.client.core.event.EventBus;
import com.auction.client.core.event.EventType;
import com.auction.client.core.session.ClientSessionManager;
import com.auction.client.core.ui.AlertHelper;
import com.auction.client.core.ui.SceneNavigator;
import com.auction.client.core.ui.ScenePaths;
import com.auction.client.network.SocketClient;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MainClient extends Application {

  @Override
  public void start(Stage stage) {
    SceneNavigator.setStage(stage);
    stage.setTitle("Auction Client");
    stage.setOnCloseRequest(event -> ClientSessionManager.shutdownApplicationSession());

    registerGlobalEventHandlers();
    SceneNavigator.switchScene(ScenePaths.LOGIN);
    connectInBackground();
  }

  private void registerGlobalEventHandlers() {
    EventBus.getInstance()
        .subscribe(
            EventType.CONNECTION_LOST,
            evt ->
                Platform.runLater(
                    () ->
                        AlertHelper.showError(
                            "Mất kết nối",
                            "Kết nối đến server đã bị ngắt. Vui lòng kiểm tra lại mạng/server.")));
  }

  private void connectInBackground() {
    Thread connectThread =
        new Thread(
            () -> {
              try {
                SocketClient.getInstance().connect();
              } catch (IOException e) {
                Platform.runLater(
                    () ->
                        AlertHelper.showError(
                            "Kết nối thất bại",
                            "Không thể kết nối đến server. Vui lòng kiểm tra lại server rồi thử đăng nhập lại."));
              }
            },
            "socket-connect-thread");

    connectThread.setDaemon(true);
    connectThread.start();
  }

  @Override
  public void stop() {
    ClientSessionManager.shutdownApplicationSession();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
