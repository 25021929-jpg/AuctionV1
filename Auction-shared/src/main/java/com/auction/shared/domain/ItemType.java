package com.auction.shared.domain;

/**
 * High-level product types required by the auction project statement.
 *
 * <p>The database stores these selectable types in table {@code categories}; this enum exists as a
 * shared vocabulary so Client/Server documentation and seed data stay aligned with the required
 * high-level item families.
 */
public enum ItemType {
  ELECTRONICS,
  ART,
  VEHICLE
}
