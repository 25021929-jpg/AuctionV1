package com.auction.client.core.util;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JsonReadTest {

  @Test
  void optLong_returnsValueFromFirstExistingKey() {
    JsonObject o = new JsonObject();
    o.addProperty("id", 10);
    o.addProperty("auctionId", 99);

    Long v = JsonRead.optLong(o, "auctionId", "id");
    assertEquals(99L, v);
  }

  @Test
  void optLong_parsesNumericString() {
    JsonObject o = new JsonObject();
    o.addProperty("id", "123");
    assertEquals(123L, JsonRead.optLong(o, "id"));
  }

  @Test
  void optDouble_parsesNumericString() {
    JsonObject o = new JsonObject();
    o.addProperty("price", "456.5");
    assertEquals(456.5, JsonRead.optDouble(o, "price"));
  }

  @Test
  void optDouble_returnsNullWhenMissing() {
    JsonObject o = new JsonObject();
    assertNull(JsonRead.optDouble(o, "price"));
  }

  @Test
  void optDateTime_parsesIsoString() {
    JsonObject o = new JsonObject();
    o.addProperty("endTime", "2026-05-25T10:15:30");

    LocalDateTime dt = JsonRead.optDateTime(o, "endTime");
    assertNotNull(dt);
    assertEquals(2026, dt.getYear());
    assertEquals(5, dt.getMonthValue());
    assertEquals(25, dt.getDayOfMonth());
  }
}
