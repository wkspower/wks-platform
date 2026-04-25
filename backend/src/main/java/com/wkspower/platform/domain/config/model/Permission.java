package com.wkspower.platform.domain.config.model;

import java.util.Optional;

/**
 * Role permission verbs recognised in Phase 0 YAML. Enforcement is Story 5.2's concern — this enum
 * is purely the validation allow-list. Unknown verbs produce {@code WKS-CFG-008}.
 */
public enum Permission {
  VIEW,
  CREATE,
  TRANSITION,
  ASSIGN,
  UPLOAD_DOCUMENT,
  DELETE,
  EXPORT,
  EDIT,
  COMMENT,
  ADMIN;

  public String wire() {
    return name().toLowerCase().replace('_', '-');
  }

  public static Optional<Permission> fromWire(String wire) {
    if (wire == null) {
      return Optional.empty();
    }
    for (Permission p : values()) {
      if (p.wire().equals(wire)) {
        return Optional.of(p);
      }
    }
    return Optional.empty();
  }
}
