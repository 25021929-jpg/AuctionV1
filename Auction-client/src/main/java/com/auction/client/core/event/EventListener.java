package com.auction.client.core.event;

@FunctionalInterface
public interface EventListener {
  void onEvent(AppEvent event);
}
