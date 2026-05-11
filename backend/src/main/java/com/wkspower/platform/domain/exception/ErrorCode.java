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
  /**
   * Story 3.11 — admin deploy issued with {@code ?force=true} but the request violates a
   * force-override precondition: either the active Spring profile is {@code production}
   * (force-override is policy-forbidden in prod regardless of caller authority) or {@code
   * bumpVersion=true} was not supplied (force-override requires explicit version bump). Emitted by
   * {@link com.wkspower.platform.api.controller.AdminController#deploy}. Wire string is {@code
   * WKS-API-006} — stable contract; do not reuse for another meaning per the {@code
   * feedback_error_codes_are_wire_contract.md} memory.
   */
  WKS_API_006("WKS-API-006"),
  /**
   * Story 3.9 — rebase request supplied an invalid {@code toVersion} argument, or the case's bound
   * {@code caseTypeId} does not match the path parameter. Covers: {@code toVersion} equal to or
   * less than current version (forward-only constraint), {@code toVersion} not found in {@code
   * case_type_versions}, non-integer or missing {@code to} param, and caseTypeId mismatch between
   * path and case row. Wire string is {@code WKS-API-007} — stable contract per {@code
   * feedback_error_codes_are_wire_contract.md}; never reuse for a different meaning.
   */
  WKS_API_007("WKS-API-007"),

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
   * Story 4.4a AC5 — BPMN userTask sits on a CaseType with stage-scoped statuses but lacks an
   * explicit {@code <camunda:property name="status">} declaration. The legacy Phase-0 fallback
   * (pick {@code getActiveActivityIds → first non-self}) is REMOVED in Story 4.4a because it breaks
   * under parallel gateways — fall-through silence becomes a deploy-time hard failure instead.
   * Reuse of this code for any other meaning is forbidden per {@code
   * feedback_error_codes_are_wire_contract.md}.
   */
  WKS_CFG_024("WKS-CFG-024"),
  /**
   * Story 4.5 AC1 — BPMN engine deployment failure during atomic deploy. Emitted by {@link
   * com.wkspower.platform.domain.service.ConfigService#deploy} when the engine deploy step (step 5
   * of the AC1 ordered sequence) throws an exception AFTER validation succeeded. The registry is
   * NOT written when this code is returned — no orphan row in {@code case_type_versions}. HTTP 502
   * / 500 depending on caller context. Reuse for any other meaning is forbidden per {@code
   * feedback_error_codes_are_wire_contract.md}.
   */
  WKS_CFG_025("WKS-CFG-025"),
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
   * Mutate-class CaseType change attempted without supplying {@code bumpVersion=true} (Story 3.8).
   *
   * <p>Story 3.8 emission site: {@code ConfigService.validateAndRegister} and {@code
   * ConfigService.deploy} invoke {@link
   * com.wkspower.platform.domain.config.diff.CaseTypeDiff#classify}; this code is emitted when
   * {@code mutateDeltas} is non-empty and {@code bumpVersion=true} was not supplied on the deploy
   * request. The response envelope's {@code meta.blastRadius} field carries the full {@link
   * com.wkspower.platform.domain.config.diff.BlastRadiusReport} for Admin UI rendering.
   *
   * <p>{@code MappingValidator} does not emit this code — the constant was reserved in Story 4.2 as
   * a cross-reference anchor; Story 3.8 is the first emission site. Wire string is {@code
   * WKS-CFG-029} — stable contract.
   */
  WKS_CFG_029("WKS-CFG-029"),
  /**
   * Blast-radius gate could not load or re-parse the prior {@code
   * case_type_versions.definition_yaml} for a CaseType whose {@code currentVersion()} is present
   * (Story 3.8 PR #417 follow-up).
   *
   * <p>Emitted by {@code ConfigService.runBlastRadiusGate} when the prior YAML row is missing, has
   * null bytes, or fails to re-parse — any state in which the AC2 invariant ("the gate applies on
   * every deploy that has a prior version") cannot be honored. The deploy is rejected fail-closed
   * rather than silently bypassing the classifier.
   *
   * <p>Wire string is {@code WKS-CFG-030} — stable contract; do not reuse for another meaning per
   * the {@code feedback_error_codes_are_wire_contract.md} memory.
   */
  WKS_CFG_030("WKS-CFG-030"),
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
  /**
   * Story 3.9 — rebase apply aborted because the dry-run report contains one or more irreconcilable
   * items (e.g. a removed field has non-null data on the case, or the current status has no
   * equivalent in the target version). The apply is rejected atomically; {@code
   * cases.case_type_version} is unchanged. The error response body includes the full {@code
   * irreconcilable} list so the operator can identify items requiring manual decision before
   * retrying. Wire string is {@code WKS-CFG-034} — next free slot after 031/032/033 already minted
   * by Sprint 9 stories. Stable contract per {@code feedback_error_codes_are_wire_contract.md};
   * never reuse for a different meaning.
   */
  WKS_CFG_034("WKS-CFG-034"),
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
   * Story 4.3.1 AC8 — mapping YAML carries an unknown key (typo; e.g. {@code events.signl:}, {@code
   * emits: { typ: status }}). Emitted at deploy time by {@link
   * com.wkspower.platform.infrastructure.config.MappingValidator} (and the YAML loader when the
   * unknown property surfaces during Jackson deserialization). Carries the file path + JSON pointer
   * + offending key name so the operator can locate the typo without re-reading the YAML.
   */
  WKS_MAP_008("WKS-MAP-008"),
  /**
   * Story 4.3.1 AC9 — duplicate map key in mapping YAML (e.g. {@code userTasks.review-claim:}
   * appearing twice). Default Jackson behavior is silent last-wins; this code surfaces the
   * duplicate via {@code STRICT_DUPLICATE_DETECTION} so an operator never silently loses a rule.
   */
  WKS_MAP_009("WKS-MAP-009"),
  /**
   * Story 6.2 AC5 — parse-time: a {@code routing.outcomes.<key>} rule in a mapping YAML references
   * an outcome key that is not declared in any {@code userTasks.<id>.outcomes: []} list for that
   * attachment. Emitted by {@link com.wkspower.platform.infrastructure.config.MappingValidator} at
   * deploy time (before the runtime router ever sees the configuration). The operator must either
   * add the key to the appropriate {@code userTasks.<id>.outcomes} list or remove the orphaned
   * routing rule.
   *
   * <p>Per {@code feedback_error_codes_are_wire_contract.md}: the wire string {@code "WKS-MAP-010"}
   * is a stable contract; never reuse for any other meaning.
   */
  WKS_MAP_010("WKS-MAP-010"),
  /**
   * Story 6.2 — parse-time: an attachment declares a {@code routing.outcomes:} block but no {@code
   * routing.userTasks.<id>.outcomes:} list references any outcome key. The block is unused. The
   * operator must either remove the orphaned {@code outcomes:} block or add the relevant outcome
   * keys to a userTask's {@code outcomes:} list. Emitted by {@link
   * com.wkspower.platform.infrastructure.config.MappingValidator}.
   *
   * <p>Per {@code feedback_error_codes_are_wire_contract.md}: the wire string {@code "WKS-MAP-011"}
   * is a stable contract; never reuse for any other meaning.
   */
  WKS_MAP_011("WKS-MAP-011"),
  /**
   * Story 6.2 — parse-time: a {@code routing.userTasks.<id>.outcomes:} list declares an outcome key
   * that has no corresponding {@code routing.outcomes.<key>} rule. The key is bound to a userTask
   * but cannot be routed — the dispatch would always miss with {@code WKS-MAP-404} at runtime.
   * Inverse of {@link #WKS_MAP_010} (rule without declaration). Emitted by {@link
   * com.wkspower.platform.infrastructure.config.MappingValidator}.
   *
   * <p>Per {@code feedback_error_codes_are_wire_contract.md}: the wire string {@code "WKS-MAP-012"}
   * is a stable contract; never reuse for any other meaning.
   */
  WKS_MAP_012("WKS-MAP-012"),
  /**
   * Runtime — emitted by {@link com.wkspower.platform.domain.service.ExecutionSignalRouter} when an
   * incoming {@link com.wkspower.platform.domain.port.ExecutionSignal} does not match any rule in
   * the active {@link com.wkspower.platform.domain.config.model.MappingDefinition}, when the
   * CaseInstance's pinned {@code (caseTypeId, version)} is missing from {@link
   * com.wkspower.platform.domain.service.MappingRegistry}, or when a property emission attempts to
   * drive a stage transition (Story 4.3 AC2 / AC4 / AC9). Distinct from deploy-time {@link
   * #WKS_MAP_001}..{@link #WKS_MAP_006}; the {@code 404} number intentionally mirrors HTTP
   * not-found semantics for "rule not found." Per {@code
   * feedback_error_codes_are_wire_contract.md}: the wire string is stable; never reuse for any
   * other meaning.
   */
  WKS_MAP_404("WKS-MAP-404"),
  /**
   * Story 4.3.1 AC5 — runtime: {@link com.wkspower.platform.domain.service.ExecutionSignalRouter}
   * received a {@link com.wkspower.platform.domain.port.ExecutionSignal} for a {@code caseId} that
   * is no longer present in the case repository (purged, hot-reload race, adapter sending after
   * case deletion). Distinct from {@code WKS-MAP-404} (rule miss); {@code -405} mirrors the HTTP
   * "method not allowed / case-not-found" suffix convention. Audited via {@code
   * ExecutionSignalRouted} with {@code source = backend(<adapter>)}; never silently dropped.
   */
  WKS_MAP_405("WKS-MAP-405"),

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

  // Story 3.6 — stage-scoped status set deploy-time + transition-time codes.
  // Band: WKS-STG-005..011 consumed; -009 reserved for Story 3.7 / 3.8 mutate-class CRUD.
  /** Duplicate status id within a stage's status set (Story 3.6 AC3). HTTP 422. */
  WKS_STG_005("WKS-STG-005"),
  /**
   * Stage's {@code initialStatus:} references an id not present in the stage's status set (Story
   * 3.6 AC3). HTTP 422.
   */
  WKS_STG_006("WKS-STG-006"),
  /**
   * Story 3.7 AC1 — admin append rejected because the requested {@code statusId} already exists on
   * {@code (case_type_id, current-version, stage_id)}. The stage's existing status set already
   * declares this id (either from the deploy-time YAML or a previous append). HTTP 409.
   *
   * <p>Surfaced by the admin POST {@code .../stages/{stageId}/statuses} path (Story 3.7); never
   * emitted at deploy-time (deploy duplicates surface as {@code WKS-STG-005}).
   */
  WKS_STG_007("WKS-STG-007"),
  /**
   * Stage's {@code statuses: []} is empty (key declared but empty list — must be {@code >= 1} or
   * omit the key entirely to fall back to the flat set per Story 3.6 AC2). HTTP 422.
   */
  WKS_STG_008("WKS-STG-008"),
  /**
   * Story 3.7 AC1 — admin mutate-class rejection: DELETE on a status, or PATCH that flips the
   * {@code terminal} flag, is rejected because removal / terminal-flag changes require Story 3.8's
   * mutate-class version-bump envelope. Reserved by Story 3.6; first emitted by Story 3.7's admin
   * controller. HTTP 405 Method Not Allowed.
   */
  WKS_STG_009("WKS-STG-009"),
  /**
   * Story 3.6 AC6 — transition request targets a status id that is not declared on the case's
   * current stage's status set. The Story 3.6 dev story originally suggested reusing {@code
   * WKS-STG-002}; that code is wire-locked by Story 3.1 ("backward skip"), so a fresh code lands
   * here per {@code feedback_error_codes_are_wire_contract.md}. HTTP 422.
   */
  WKS_STG_010("WKS-STG-010"),
  /**
   * Story 3.6 AC6 — transition rejected because the case's current status declares {@code terminal:
   * true} on the active stage. Same-stage transitions are blocked; advance the stage to continue.
   * The Story 3.6 dev story originally suggested reusing {@code WKS-STG-001}; that code is
   * wire-locked by Story 3.1 ("advance/skip on completed/zero-stage case"). Fresh code per {@code
   * feedback_error_codes_are_wire_contract.md}. HTTP 422.
   */
  WKS_STG_011("WKS-STG-011"),
  /**
   * Story 3.7 AC1 — admin status CRUD path resolved an unknown {@code caseTypeId} or {@code
   * stageId}. Distinct from {@code WKS-API-404} (generic resource-not-found): the wire code
   * disambiguates "the case-type lookup failed" vs "the stage lookup failed" via the message text;
   * SI runbooks grep on {@code WKS-STG-012} for "admin tried to edit statuses on a non-existent
   * stage". Distinct from {@code WKS-STG-004} (runtime advance/skipTo on unknown caseId) — that is
   * a CASE id, this is a CASETYPE id. HTTP 404.
   */
  WKS_STG_012("WKS-STG-012"),
  /**
   * Story 3.7 AC1 — admin {@code PATCH .../statuses/{statusId}} rename targeted a status id that is
   * not currently declared on the stage's status set (neither in the frozen-on-version YAML base
   * nor in the {@code status_options} append-class delta). Distinct from {@code WKS-STG-012} (the
   * stage itself is unknown). HTTP 404.
   */
  WKS_STG_013("WKS-STG-013"),

  // 503 — CaseType Version Registry (Story 3.4 / Decision 20; HTTP semantic flipped by Story
  // 3.4.1).
  // Band: WKS-VER-001..099 RESERVED for version-registry-related errors. Future stories (3.5
  // bootstrap, 3.8 blast-radius validator, 3.9 rebase tooling) draw from this band per
  // feedback_error_codes_are_wire_contract.md. Never reuse a code in this band for a different
  // meaning — the wire is a contract.
  /**
   * {@code CaseService.create} called for a CaseType that is registry-visible (in-memory) but has
   * no row in {@code case_type_versions} yet — partial-failure recovery state, registry-not-
   * yet-primed, or startup race. HTTP 503 (Retry-After: 5) — Story 3.4.1 AC4 (finding I6) flipped
   * this from 409, which misleads as "client conflict." Story 3.4 introduces this code; Story 3.5's
   * bootstrap migration closes the gap for pre-3.4 CaseTypes by materialising v1 rows.
   */
  WKS_VER_001("WKS-VER-001"),

  // 422 — Form Definition Schema validation (Story 5.1). Band: WKS-FORM-001..099.
  // Codes 002+ are RESERVED for Stories 5.2–5.8 — do NOT mint speculatively.
  /**
   * Story 5.1 AC1 — {@code topology: parallel} (or any non-{@code single} topology value) is a
   * Phase-1 capability and is rejected in Phase 0. Error message: {@code "topology: parallel is a
   * Phase-1 capability — use topology: single"}. The wire string is stable; future stories that
   * support additional topologies must not reuse this code for a different meaning per {@code
   * feedback_error_codes_are_wire_contract.md}.
   */
  WKS_FORM_001("WKS-FORM-001"),
  /**
   * Story 5.2 AC3 — form submit field-level validation failure (backend-side). Emitted by {@link
   * com.wkspower.platform.domain.service.CaseService#submitForm} when a submitted field value fails
   * a type-specific constraint not caught by the frontend Zod schema. One error detail per
   * offending field; the {@code field} slot in the error detail carries the field id. HTTP 422.
   *
   * <p>Per {@code feedback_error_codes_are_wire_contract.md}: the wire string is stable and must
   * never be reused for a different meaning.
   */
  WKS_FORM_002("WKS-FORM-002"),
  /**
   * Story 5.2 — form submission body is null or empty. Emitted by {@link
   * com.wkspower.platform.api.controller.FormController} when the {@code @RequestBody} is absent or
   * resolves to an empty map (blank-case-data attack guard). HTTP 400.
   *
   * <p>Per {@code feedback_error_codes_are_wire_contract.md}: the wire string is stable and must
   * never be reused for a different meaning.
   */
  WKS_FORM_003("WKS-FORM-003"),

  // 422 — Per-field edit permission band (Story 5.6). Band: WKS-AUTHZ-*. First member.
  /**
   * Story 5.6 — Form-submit attempted to write a field whose {@code editableBy} declaration
   * excludes the actor's role set. Emitted by {@link
   * com.wkspower.platform.domain.service.CaseService#submitForm} when a submitted-and-changed
   * field's permission check fails. Multi-error envelope: one {@link ErrorDetail} per offending
   * field.
   *
   * <p>Distinct from {@link #WKS_API_403} (caller-level authorization — entire endpoint denied) and
   * {@link #WKS_FORM_002} (field-value validation — wrong type/range). This code says: the value
   * itself was valid; the actor was not allowed to change it.
   *
   * <p>Wire string is {@code WKS-AUTHZ-001} — stable contract. First member of the new {@code
   * WKS-AUTHZ-*} band; do not reuse for any other meaning per {@code
   * feedback_error_codes_are_wire_contract.md}. HTTP 422 — rides the existing {@code
   * WksValidationAggregateException} carrier so no new mapping is needed in {@code
   * GlobalExceptionHandler}.
   *
   * <p>Note — the Story 5.6 dev notes prescribed the wire literal {@code WKS-AUTHZ-FIELD}, but the
   * existing {@code WKS-&lt;PREFIX&gt;-NNN} format invariant (enforced by {@code
   * ErrorCodeTest.wireStringsFollowWksHyphenFormat}) requires a 3-digit numeric suffix. The Java
   * identifier preserves the {@code WKS_AUTHZ_FIELD} mnemonic; the wire string follows the format.
   */
  WKS_AUTHZ_FIELD("WKS-AUTHZ-001"),

  // 409 — runtime conflict.
  /**
   * Optimistic-locking conflict on update — the row was modified by another transaction between
   * read and write. Story 2.3 introduces this code; produced by {@code
   * GlobalExceptionHandler.handleOptimisticLock} and {@code WksConflictException}.
   */
  WKS_RTM_409("WKS-RTM-409"),

  // 500.
  /** Uncaught exception — last resort. */
  WKS_RTM_500("WKS-RTM-500"),

  // 422 / 404 / 502 — Document upload and storage (Story 14.2). Band: WKS-DOC-001..099.
  /**
   * File size exceeds the configured maximum (default 25 MB, property {@code
   * wks.documents.max-size-mb}). HTTP 422.
   */
  WKS_DOC_001("WKS-DOC-001"),
  /** Content type not in the upload allowlist. HTTP 422. */
  WKS_DOC_002("WKS-DOC-002"),
  /**
   * Filename rejected — path traversal ({@code ../}, directory separators) or executable extension
   * ({@code .exe}, {@code .sh}, {@code .bat}, {@code .cmd}, {@code .ps1}, {@code .js}, {@code
   * .py}). HTTP 422.
   */
  WKS_DOC_003("WKS-DOC-003"),
  /**
   * Document not found — the requested {@code documentId} references no row in {@code
   * case_documents}. HTTP 404.
   */
  WKS_DOC_004("WKS-DOC-004"),
  /**
   * Storage backend error — file retrieve or store failed (MinIO unreachable, file missing from
   * local store). HTTP 502.
   */
  WKS_DOC_005("WKS-DOC-005"),

  // License band (Story 7.1). WKS-LIC-NNN is a new prefix. Codes are INFO/WARN-level
  // operational states, not HTTP errors to callers — the platform never hard-fails on license
  // problems (AR-D24). Band: WKS-LIC-001..099 (only 001 and 002 allocated in 7.1; future stories
  // 7.2..7.7 draw from this band). Per feedback_error_codes_are_wire_contract.md: never reuse.
  /**
   * License file path is configured but the file is missing or unreadable. Platform boots in OSS
   * mode (INFO-level; not an exception to callers). Story 7.1 AC4.
   */
  WKS_LIC_001("WKS-LIC-001"),
  /**
   * License JWT signature is invalid, JWT is malformed, or the {@code tier} / {@code features}
   * claims are absent. Platform boots in degraded state (WARN-level; not an exception to callers).
   * Story 7.1 AC3.
   */
  WKS_LIC_002("WKS-LIC-002"),
  /**
   * SSO/SAML endpoint accessed while the {@code auth.sso} feature is disabled by the current
   * license (OSS, Team, Expired, or Degraded state). The platform responds 404 — the endpoint does
   * not exist for this license tier. HTTP 404. Story 7.5 AC1.
   */
  WKS_LIC_003("WKS-LIC-003"),
  /**
   * SSO/SAML availability could not be determined because the {@code LicenseService} threw while
   * being consulted by {@code SamlGatingFilter}. The filter fails CLOSED (404, treats SSO as
   * unavailable) and logs the underlying exception at WARN. HTTP 404. Story 7.5 review remediation.
   */
  WKS_LIC_004("WKS-LIC-004"),

  // ---------------------------------------------------------------------------
  // Archetype validation band (Story 6.1) — YAML-surface archetype violations.
  // Distinct from BPMN-side WKS-CFG-020 (missing) and WKS-CFG-021 (contradiction);
  // this code is the YAML-surface unknown-value violation.
  // Per feedback_error_codes_are_wire_contract.md: the wire string is stable and must
  // never be reused for a different meaning.
  // ---------------------------------------------------------------------------
  /**
   * Story 6.1 — YAML-declared archetype on a task / form / stage references a value outside the
   * closed Phase-0 catalog ({@code draft_section}, {@code submit_for_processing}, {@code
   * business_final}). Distinct from BPMN-side {@link #WKS_CFG_020} (missing) and {@link
   * #WKS_CFG_021} (contradiction); this code is the YAML-surface unknown-value violation.
   */
  WKS_ARCH_001("WKS-ARCH-001"),
  /**
   * Story 6.2 — runtime: emitted by {@link
   * com.wkspower.platform.engine.listeners.CaseStatusListener} when a BPMN userTask carries BOTH an
   * {@code outcome} process variable AND a {@code <camunda:property name="status">} declaration.
   * The {@code outcome} variable always wins (drives multi-outcome routing); the {@code status}
   * property is shadowed. WARN-level — never aborts the dispatch. Operator action: remove one of
   * the two declarations to disambiguate intent.
   *
   * <p>Per {@code feedback_error_codes_are_wire_contract.md}: the wire string {@code
   * "WKS-ROUTE-001"} is a stable contract; never reuse for any other meaning.
   */
  WKS_ROUTE_001("WKS-ROUTE-001"),
  /**
   * Story 6.2 — runtime: emitted by {@link
   * com.wkspower.platform.domain.service.ExecutionSignalRouter#resetStatusForAdvancedStage} when a
   * stage advance succeeds but the next stage declares no statuses AND no top-level fallback
   * applies — the case's existing status persists across the stage boundary. WARN-level; surfaces a
   * stale-status condition that may indicate a missing {@code statuses:} declaration on the next
   * stage. Operator action: either declare the next stage's statuses or set top-level statuses to
   * seed the post-advance value.
   *
   * <p>Per {@code feedback_error_codes_are_wire_contract.md}: the wire string {@code
   * "WKS-STAT-001"} is a stable contract; never reuse for any other meaning.
   */
  WKS_STAT_001("WKS-STAT-001");

  private final String wire;

  ErrorCode(String wire) {
    this.wire = wire;
  }

  /** Returns the public wire string (e.g. {@code "WKS-API-003"}). */
  public String wire() {
    return wire;
  }
}
