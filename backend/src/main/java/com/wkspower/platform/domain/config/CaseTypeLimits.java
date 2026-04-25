package com.wkspower.platform.domain.config;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Phase 0 hard limits for case-type YAML, centralised so the validator and any future admin UI read
 * the same numbers. No scattered magic values.
 */
public final class CaseTypeLimits {

  private CaseTypeLimits() {}

  public static final int MAX_FIELDS = 50;
  public static final int MAX_LIST_COLUMNS = 12;
  public static final int MAX_STATUSES = 10;
  public static final int MAX_DISPLAY_NAME_CHARS = 40;
  public static final int MAX_DESCRIPTION_CHARS = 400;
  public static final int MAX_ROLES = 20;
  public static final int MAX_PERMISSIONS_PER_ROLE = 10;
  public static final int MAX_SELECT_OPTIONS = 50;
  public static final int MIN_SELECT_OPTIONS = 1;

  /**
   * Kebab-case id (case type, status, role) — field ids also accept snake_case (see
   * FIELD_ID_PATTERN).
   */
  public static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-]{1,62}");

  /** Field ids may be kebab- or snake-case. */
  public static final Pattern FIELD_ID_PATTERN = Pattern.compile("[a-z][a-z0-9_-]{1,62}");

  /** System-column ids permitted in {@code listColumns} alongside field ids. */
  public static final Set<String> SYSTEM_LIST_COLUMNS =
      Set.of("id", "createdAt", "updatedAt", "status", "assignee");
}
