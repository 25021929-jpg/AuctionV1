package com.auction.client.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Utility đọc Json theo kiểu tolerant (không phụ thuộc schema cứng).
 *
 * <p>Khi server thay đổi key/format, chỉ cần sửa tập trung ở đây hoặc ở service mapping.
 */
public final class JsonRead {

  private JsonRead() {}

  public static Long optLong(JsonObject o, String... keys) {
    for (String k : keys) {
      try {
        if (!o.has(k) || o.get(k).isJsonNull()) continue;

        JsonElement el = o.get(k);
        // number
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
          return el.getAsLong();
        }
        // numeric string
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
          String v = el.getAsString();
          try {
            return Long.parseLong(v.trim());
          } catch (Exception ignored) {
            // ignore
          }
        }
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  public static Double optDouble(JsonObject o, String... keys) {
    for (String k : keys) {
      try {
        if (!o.has(k) || o.get(k).isJsonNull()) continue;

        JsonElement el = o.get(k);
        // number
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
          return el.getAsDouble();
        }
        // numeric string
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
          String v = el.getAsString();
          try {
            return Double.parseDouble(v.trim());
          } catch (Exception ignored) {
            // ignore
          }
        }
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  public static BigDecimal optBigDecimal(JsonObject o, String... keys) {
    for (String k : keys) {
      try {
        if (!o.has(k) || o.get(k).isJsonNull()) continue;

        JsonElement el = o.get(k);
        if (el.isJsonPrimitive()
            && (el.getAsJsonPrimitive().isNumber() || el.getAsJsonPrimitive().isString())) {
          String raw = el.getAsString();
          if (raw != null && !raw.isBlank()) {
            return new BigDecimal(raw.trim());
          }
        }
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  public static String optString(JsonObject o, String... keys) {
    for (String k : keys) {
      try {
        if (o.has(k) && !o.get(k).isJsonNull()) {
          return o.get(k).getAsString();
        }
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  /**
   * Chấp nhận: - epoch millis (number) - ISO-8601 LocalDateTime string - epoch millis dạng string
   */
  public static LocalDateTime optDateTime(JsonObject o, String... keys) {
    for (String k : keys) {
      try {
        if (!o.has(k) || o.get(k).isJsonNull()) continue;
        JsonElement el = o.get(k);
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
          long epochMillis = el.getAsLong();
          return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
        }
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
          String s = el.getAsString();
          try {
            return LocalDateTime.parse(s);
          } catch (Exception ignored) {
            try {
              long epochMillis = Long.parseLong(s);
              return LocalDateTime.ofInstant(
                  Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
            } catch (Exception ignored2) {
              return null;
            }
          }
        }
      } catch (Exception ignored) {
      }
    }
    return null;
  }
}
