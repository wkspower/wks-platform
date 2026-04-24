package com.wkspower.platform.domain.config.model;

import java.util.Optional;

/**
 * Ten palette tokens allowed on a status. Locked in by Decision 11. Unknown tokens produce {@code
 * WKS-CFG-008}.
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

  public String wire() {
    return name().toLowerCase();
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
