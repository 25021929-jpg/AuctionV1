package com.auction.shared.protocol;

/**
 * Phân loại message trên wire (1-line JSON).
 *
 * <p>Phương án 1: mọi message đều là 1 dòng JSON, có field {@code type} để phân biệt
 * REQUEST/RESPONSE/EVENT.
 */
public enum WireMessageType {
  /** Client -> Server */
  REQUEST,
  /** Server -> Client (reply theo requestId) */
  RESPONSE,
  /** Server -> Client (push realtime) */
  EVENT
}
