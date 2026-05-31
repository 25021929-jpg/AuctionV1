package com.auction.client.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * EventBus đơn giản (Observer Pattern).
 *
 * <p>Lưu ý: Realtime Update yêu cầu không polling. Khi server support push event, SocketClient sẽ
 * nhận event và publish vào đây.
 */
public final class EventBus {

  private static volatile EventBus instance;

  private final Map<EventType, List<EventListener>> listeners = new ConcurrentHashMap<>();

  private EventBus() {}

  public static EventBus getInstance() {
    if (instance == null) {
      synchronized (EventBus.class) {
        if (instance == null) {
          instance = new EventBus();
        }
      }
    }
    return instance;
  }

  public void subscribe(EventType type, EventListener listener) {
    if (type == null || listener == null) {
      return;
    }
    List<EventListener> list = listeners.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>());
    if (!list.contains(listener)) {
      list.add(listener);
    }
  }

  public void unsubscribe(EventType type, EventListener listener) {
    if (type == null || listener == null) {
      return;
    }
    List<EventListener> list = listeners.get(type);
    if (list != null) {
      list.remove(listener);
      if (list.isEmpty()) {
        listeners.remove(type, list);
      }
    }
  }

  public void publish(AppEvent event) {
    if (event == null || event.type() == null) {
      return;
    }
    List<EventListener> list = listeners.get(event.type());
    if (list == null) return;
    for (EventListener listener : list) {
      try {
        listener.onEvent(event);
      } catch (RuntimeException ignored) {
        // Một listener lỗi không được làm hỏng các listener còn lại.
      }
    }
  }
}
