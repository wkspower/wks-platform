package com.wkspower.platform.domain.exception;

/**
 * Centralised registry of WKS error codes. Each constant carries the stable wire string surfaced to
 * clients via the {@code ApiResponse.error.code} envelope field. Grep-friendly on purpose — the
 * flat shape (no sub-enums) keeps "find every code" a one-line search.
 *
 * <p>The wire string is the source of truth for the public contract; the Java identifier is
 * internal. Never renumber or reuse a code once shipped — front-end snapshot tests and customer
 * runbooks depend on exact strings.
 *
 * <p>Mappings to HTTP status live in {@link com.wkspower.platform.api.GlobalExceptionHandler}, not
 * here, so this enum stays transport-free and consumable from domain code.
 */
public enum ErrorCode {

  // 400 — malformed requests.
  /** Request body malformed or unreadable. */
  WKS_API_001("WKS-API-001"),
  /** JSON parse error raised by Jackson. */
  WKS_API_002("WKS-API-002"),
  /** Pagination parameter out of range ({@code size > 100}, {@code size < 1}, {@code page < 0}). */
  WKS_API_003("WKS-API-003"),
  /** Sort property not declared in the resource allow-list. */
  WKS_API_004("WKS-API-004"),
  /** Sort direction other than {@code asc} or {@code desc}. */
  WKS_API_005("WKS-API-005"),

  // 401 / 403 — auth.
  /** Authentication failed (unknown email, wrong password, or inactive user). */
  WKS_API_401("WKS-API-401"),
  /** Authenticated caller lacks authority for the operation. */
  WKS_API_403("WKS-API-403"),

  // 404.
  /** Resource not found. */
  WKS_API_404("WKS-API-404"),

  // 422 — multi-error config aggregate (Story 2.2 deploy endpoint reuses this).
  /** Umbrella code for multi-error configuration / deploy aggregates. */
  WKS_CFG_000("WKS-CFG-000"),

  // 413 — multipart upload caps (Story 2.2 admin deploy endpoint).
  /**
   * Multipart part exceeded the configured cap (1 MB per part / 2 MB per request). Field carries
   * the offending part name when extractable.
   */
  WKS_API_413("WKS-API-413"),

  // 422 — case-type YAML validation band (Story 2.1). Codes 010, 012–021 are
  // reserved for Story 2.2 (BPMN validation) — leave gaps, do not renumber.
  /** Required key missing (top-level or nested). */
  WKS_CFG_001("WKS-CFG-001"),
  /** Invalid field {@code type} — not one of the seven. */
  WKS_CFG_002("WKS-CFG-002"),
  /** Duplicate id (field / status / role / listColumn entry). */
  WKS_CFG_003("WKS-CFG-003"),
  /** {@code fields.length > 50}. */
  WKS_CFG_004("WKS-CFG-004"),
  /** {@code listColumns.length > 12} OR references unknown field id / unknown system column. */
  WKS_CFG_005("WKS-CFG-005"),
  /** {@code statuses.length > 10}. */
  WKS_CFG_006("WKS-CFG-006"),
  /** Any {@code displayName} exceeds 40 characters. */
  WKS_CFG_007("WKS-CFG-007"),
  /** Unknown enum literal (status color, role permission verb). */
  WKS_CFG_008("WKS-CFG-008"),
  /** Malformed id — fails {@code [a-z][a-z0-9-]{1,62}} (or {@code [_-]} for field ids). */
  WKS_CFG_009("WKS-CFG-009"),
  /** BPMN file missing, unreadable, or not a BPMN 2.0 document (Story 2.2). */
  WKS_CFG_010("WKS-CFG-010"),
  /**
   * Reserved for Story 2.2 and the registry: emitted when {@code replace} rejects an incoming
   * config whose version is lower than the currently registered one. NOT produced by the validator.
   */
  WKS_CFG_011("WKS-CFG-011"),
  /**
   * BPMN expression references a variable not declared in the YAML case-type and not in the
   * well-known set ({@code taskAssignee}, {@code caseId}, {@code caseStatus}). One per missing
   * reference (Story 2.2).
   */
  WKS_CFG_012("WKS-CFG-012"),
  /**
   * BPMN user task is missing the required {@code archetype} declaration in {@code
   * camunda:properties}. One per offending user task (Story 2.2).
   */
  WKS_CFG_020("WKS-CFG-020"),
  /**
   * BPMN archetype contradiction (e.g. {@code business_final} carries {@code
   * camunda:asyncAfter="true"}, or {@code draft_section} has a downstream task). One per offence
   * (Story 2.2).
   */
  WKS_CFG_021("WKS-CFG-021"),
  /**
   * BPMN file declares more than one executable {@code <bpmn:process>} (Story 2.2 D3).
   * Collaboration diagrams are allowed — exactly one process must be marked {@code
   * isExecutable=true}; any additional executable processes trip this code.
   */
  WKS_CFG_022("WKS-CFG-022"),
  /** YAML parse error / I/O failure (catastrophic — validator never produces). */
  WKS_CFG_099("WKS-CFG-099"),

  // 409 — runtime conflict.
  /**
   * Optimistic-locking conflict on update — the row was modified by another transaction between
   * read and write. Story 2.3 introduces this code; produced by {@code
   * GlobalExceptionHandler.handleOptimisticLock} and {@code WksConflictException}.
   */
  WKS_RTM_409("WKS-RTM-409"),

  // 500.
  /** Uncaught exception — last resort. */
  WKS_RTM_500("WKS-RTM-500");

  private final String wire;

  ErrorCode(String wire) {
    this.wire = wire;
  }

  /** Returns the public wire string (e.g. {@code "WKS-API-003"}). */
  public String wire() {
    return wire;
  }
}
