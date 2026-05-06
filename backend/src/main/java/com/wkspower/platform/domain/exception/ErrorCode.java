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
   * Case-type YAML marks a {@code file}-typed field as {@code requiredOnCreate: true} but file
   * upload is not yet supported on the create form (closes in Story 3.1 — Document Upload). The
   * case-type still loads; the validator emits this as a WARN-level finding so operators get a
   * heads-up. Story 2.7 introduces this code.
   */
  WKS_CFG_013("WKS-CFG-013"),
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
  /**
   * Mapping references a BPMN userTask id that does not exist in the attached BPMN file (Story 4.2
   * AC2 / architecture §828). Canonical wire code for {@code userTasks.<id>} and {@code
   * properties[].on=userTask:<id>} cross-reference failures. Distinguishes from {@code
   * WKS-MAP-001}, which fires on non-userTask element refs (signals, events).
   */
  WKS_CFG_027("WKS-CFG-027"),
  /**
   * Mapping {@code events.endEvent.stageTransition} (or signal stageTransition) references a stage
   * adjacency the CaseType does not declare (Story 4.2 AC2 / architecture §832). Canonical wire
   * code for stage-adjacency failures; {@code WKS-MAP-007} is reserved as an alias-not-emitted to
   * give Story 4.6's Admin UI a Mapping-namespaced label option without changing the wire string.
   */
  WKS_CFG_028("WKS-CFG-028"),
  /**
   * Mapping-class change requires a CaseType version bump (Story 4.2 AC2 / architecture §833 / D20
   * cross-ref). RESERVED in Story 4.2 — emitted by Story 3.8's blast-radius validator when {@link
   * com.wkspower.platform.infrastructure.config.MappingDiff#classify} returns {@code
   * MappingChangeClass.MUTATE_CLASS} and the deployer did not supply {@code --bump}. {@code
   * MappingValidator} does not emit this code; the constant exists here so 3.8 can reference it
   * without re-allocating.
   */
  WKS_CFG_029("WKS-CFG-029"),
  /**
   * Duplicate stage id within a case-type YAML (Story 3.1 AC1). Deploy-time validator code;
   * surfaces with a {@code stages[i].id} field path.
   */
  WKS_CFG_031("WKS-CFG-031"),
  /**
   * Stage id violates the {@code [a-z][a-z0-9-]{0,62}} pattern (Story 3.1 AC1). Same regex shape as
   * status / role ids, no underscore variant — stages live on the URL surface (Story 3.3) so they
   * stay strictly kebab-case.
   */
  WKS_CFG_032("WKS-CFG-032"),
  /**
   * Stage id collides with a reserved word (Story 3.1 AC1). Initial reserved set: {@code case},
   * {@code stage}, {@code none}, {@code all}. Extend cautiously — the wire is a contract.
   */
  WKS_CFG_033("WKS-CFG-033"),
  /** YAML parse error / I/O failure (catastrophic — validator never produces). */
  WKS_CFG_099("WKS-CFG-099"),

  // 422 — Mapping Layer wire codes (Story 4.2 AC2). Epic-namespaced sibling band of the
  // architecture-document codes WKS-CFG-027/028/029 above. Codes 001..006 are emitted by
  // MappingValidator; 007 is reserved-and-not-emitted (alias of WKS-CFG-028) to give Story 4.6's
  // Admin UI a Mapping-namespaced label without changing the wire string.
  /**
   * Mapping references a BPMN element id that does not exist in the attached BPMN file (Story 4.2
   * AC2 / epics.md AC1). Fires for non-userTask references — {@code map.events.signal.<id>} that is
   * absent in the BPMN, or an {@code endEvent} rule declared on a BPMN with zero {@code
   * <bpmn:endEvent>}. UserTask references emit {@link #WKS_CFG_027} instead.
   */
  WKS_MAP_001("WKS-MAP-001"),
  /**
   * Two mappings target the same {@code (stage, status)} from different BPMN events without an
   * explicit precedence declaration (Story 4.2 AC2 / epics.md AC2). Phase-0 disallows ambiguity;
   * Phase-1 may relax once a precedence vocabulary lands.
   */
  WKS_MAP_002("WKS-MAP-002"),
  /**
   * Mapping {@code scope: stage:<id>} (or {@code emits.scope: stage:<id>}) references a stage that
   * does not exist on the CaseType (Story 4.2 AC2 / epics.md AC3). The validator collects every
   * dangling reference in one pass.
   */
  WKS_MAP_003("WKS-MAP-003"),
  /**
   * {@code attachments[].type} is not a known adapter kind (Story 4.2 AC2 / AC6). Phase-0 allows
   * only {@code bpmn}; future adapter kinds extend the {@link
   * com.wkspower.platform.infrastructure.config.MappingValidator} allow-list (see Story 4.9 for the
   * next allow-list addition).
   */
  WKS_MAP_004("WKS-MAP-004"),
  /**
   * {@code attachments[].file} is missing, unreadable, or fails the BPMN 2.0 sniff when {@code
   * type: bpmn} (Story 4.2 AC2 / AC3 / AC5). Also fires when the caller did not supply BPMN bytes
   * for the declared filename — the validator is I/O-free and does not fall back to a filesystem
   * search path.
   */
  WKS_MAP_005("WKS-MAP-005"),
  /**
   * Two {@code attachments} declare the same {@code scope} (Story 4.2 AC2). Phase-0 disallows for
   * clarity (e.g. two {@code scope: case} or two {@code scope: stage:underwriting} entries);
   * Phase-1 may relax once layered adapters have a precedence vocabulary.
   */
  WKS_MAP_006("WKS-MAP-006"),
  /**
   * RESERVED-AND-NOT-EMITTED in Story 4.2 (AC2 aliasing rule). Functionally an alias of {@link
   * #WKS_CFG_028} — the canonical wire code for stage-adjacency failures stays {@code WKS-CFG-028}.
   * {@code WKS-MAP-007} is reserved so Story 4.6's Admin UI Mapping Inspector can surface a
   * Mapping-namespaced label for the same rule without changing the wire string emitted by the
   * backend. The constant is not produced by any validator path. Per {@code
   * feedback_error_codes_are_wire_contract.md}: a reserved code may never be reused for a different
   * meaning.
   */
  WKS_MAP_007("WKS-MAP-007"),
  /**
   * Runtime — emitted by {@link com.wkspower.platform.domain.service.BackendSignalRouter} when an
   * incoming {@link com.wkspower.platform.domain.port.BackendSignal} does not match any rule in the
   * active {@link com.wkspower.platform.domain.config.model.MappingDefinition}, when the
   * CaseInstance's pinned {@code (caseTypeId, version)} is missing from {@link
   * com.wkspower.platform.domain.service.MappingRegistry}, or when a property emission attempts to
   * drive a stage transition (Story 4.3 AC2 / AC4 / AC9). Distinct from deploy-time {@link
   * #WKS_MAP_001}..{@link #WKS_MAP_006}; the {@code 404} number intentionally mirrors HTTP
   * not-found semantics for "rule not found." Per {@code
   * feedback_error_codes_are_wire_contract.md}: the wire string is stable; never reuse for any
   * other meaning.
   */
  WKS_MAP_404("WKS-MAP-404"),

  // 409 / 422 / 404 — Stage lifecycle runtime errors (Story 3.1 AC9). Band: WKS-STG-001..099.
  /**
   * advance / skipTo invoked on a case whose stages are already all completed, or on a zero-stage
   * case (no active stage to advance). HTTP 409.
   */
  WKS_STG_001("WKS-STG-001"),
  /** Skip target is at or below the currently-active stage ordinal (backward skip). HTTP 422. */
  WKS_STG_002("WKS-STG-002"),
  /**
   * Concurrent stage transition — two callers raced and the conditional-update lost the row. The
   * caller should reload and retry. HTTP 409.
   */
  WKS_STG_003("WKS-STG-003"),
  /** advance / skipTo references an unknown caseId. HTTP 404. */
  WKS_STG_004("WKS-STG-004"),

  // 409 / 422 — CaseType Version Registry (Story 3.4 / Decision 20).
  // Band: WKS-VER-001..099 RESERVED for version-registry-related errors. Future stories (3.5
  // bootstrap, 3.8 blast-radius validator, 3.9 rebase tooling) draw from this band per
  // feedback_error_codes_are_wire_contract.md. Never reuse a code in this band for a different
  // meaning — the wire is a contract.
  /**
   * {@code CaseService.create} called for a CaseType that is registry-visible (in-memory) but has
   * no row in {@code case_type_versions} yet — partial-failure recovery state. HTTP 409. Story 3.4
   * introduces this code; Story 3.5's bootstrap migration closes the gap for pre-3.4 CaseTypes by
   * materialising v1 rows.
   */
  WKS_VER_001("WKS-VER-001"),

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
