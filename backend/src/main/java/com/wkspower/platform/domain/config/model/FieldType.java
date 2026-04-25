package com.wkspower.platform.domain.config.model;

import java.util.Optional;

/**
 * The seven field types supported by Phase 0 case-type YAML. Wire form is the lowercase enum name;
 * any YAML mapper lives in infrastructure so this enum stays framework-free.
 */
public enum FieldType {
  TEXT,
  NUMBER,
  DATE,
  SELECT,
  CHECKBOX,
  TEXTAREA,
  FILE;

  /** Returns the canonical wire token (lowercase enum name) — e.g. {@code "text"}. */
  public String wire() {
    return name().toLowerCase();
  }

  /**
   * Maps a wire token (case-sensitive, lowercase) back to the enum. Returns {@link
   * Optional#empty()} when the token is not one of the seven — callers turn empty into {@code
   * WKS-CFG-002}.
   */
  public static Optional<FieldType> fromWire(String wire) {
    if (wire == null) {
      return Optional.empty();
    }
    for (FieldType t : values()) {
      if (t.wire().equals(wire)) {
        return Optional.of(t);
      }
    }
    return Optional.empty();
  }
}
