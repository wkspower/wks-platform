package com.wkspower.platform.domain.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

/**
 * Ten palette tokens allowed on a status. Locked in by Decision 11. Unknown tokens produce {@code
 * WKS-CFG-008}.
 *
 * <p>Wire form is the lowercase enum name (e.g. {@code "blue"}). Story 2.5 fixed Jackson
 * serialisation to honour {@link #wire()} so the frontend status-palette mapping in {@code
 * lib/statusColor.ts} can read the response verbatim.
 */
public enum StatusColor {
  BLUE,
  AMBER,
  VIOLET,
  EMERALD,
  ZINC,
  RED,
  CYAN,
  ROSE,
  INDIGO,
  TEAL;

  @JsonValue
  public String wire() {
    return name().toLowerCase();
  }

  @JsonCreator
  public static StatusColor fromJson(String wire) {
    return fromWire(wire)
        .orElseThrow(() -> new IllegalArgumentException("Unknown status color: " + wire));
  }

  public static Optional<StatusColor> fromWire(String wire) {
    if (wire == null) {
      return Optional.empty();
    }
    for (StatusColor c : values()) {
      if (c.wire().equals(wire)) {
        return Optional.of(c);
      }
    }
    return Optional.empty();
  }
}
