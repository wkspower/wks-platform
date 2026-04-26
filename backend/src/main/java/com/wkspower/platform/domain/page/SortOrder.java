package com.wkspower.platform.domain.page;

import java.util.Objects;

/**
 * Single sort directive — one property + direction. Property names are validated against a
 * resource-specific allow-list at the api layer (1.4's {@code SortWhitelist}); domain code trusts
 * the input.
 */
public record SortOrder(String property, boolean ascending) {

  public SortOrder {
    Objects.requireNonNull(property, "property");
    if (property.isBlank()) {
      throw new IllegalArgumentException("property must not be blank");
    }
  }

  public static SortOrder asc(String property) {
    return new SortOrder(property, true);
  }

  public static SortOrder desc(String property) {
    return new SortOrder(property, false);
  }
}
