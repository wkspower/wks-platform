package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.StatusOptionsStore;
import java.util.List;
import java.util.Optional;

/**
 * Story 3.7 — domain service driving the admin status CRUD endpoints. Routes append + rename writes
 * through {@link CaseTypeVersionRegistry} (Story 3.4) so the {@code status_options} row is bound to
 * the CURRENT bound version — append-class per Decision 21 never bumps the version.
 *
 * <p>Pure domain — no Spring annotations, no HTTP. The controller wires this around the resolver.
 *
 * <p>Live-propagation strategy: option (a), re-read on every call. See {@link
 * StatusOptionsResolver}.
 */
public class StatusOptionsAdminService {

  private final CaseTypeReader reader;
  private final CaseTypeVersionRegistry versionRegistry;
  private final StatusOptionsStore store;
  private final StatusOptionsResolver resolver;

  public StatusOptionsAdminService(
      CaseTypeReader reader, CaseTypeVersionRegistry versionRegistry, StatusOptionsStore store) {
    this.reader = reader;
    this.versionRegistry = versionRegistry;
    this.store = store;
    this.resolver = new StatusOptionsResolver(store);
  }

  /**
   * Resolve the current effective status list for {@code (caseTypeId, stageId)} — frozen YAML base
   * overlaid with live {@code status_options} rows for the current bound version.
   *
   * @throws StatusAdminLookupFailure when {@code caseTypeId} or {@code stageId} is unknown
   */
  public ResolvedStatusSet resolve(String caseTypeId, String stageId) {
    Lookup lk = lookup(caseTypeId, stageId);
    List<StatusDefinition> resolved = resolver.resolve(lk.caseType, stageId);
    String initial =
        lk.caseType
            .stage(stageId)
            .flatMap(s -> s.initialStatus())
            .orElseGet(() -> resolved.isEmpty() ? null : resolved.get(0).id());
    return new ResolvedStatusSet(stageId, initial, resolved);
  }

  /**
   * Append a new status to the stage. Routes through {@code CaseTypeVersionRegistry.currentVersion}
   * so the row is bound to the current bound version (append-class — no version bump).
   *
   * @throws StatusAdminLookupFailure when {@code caseTypeId} or {@code stageId} is unknown
   * @throws StatusOptionsStore.DuplicateStatusException when {@code statusId} already exists
   *     anywhere in the resolved set (YAML base OR previous append)
   */
  public StatusDefinition append(
      String caseTypeId,
      String stageId,
      String statusId,
      String displayName,
      String color,
      boolean terminal) {
    Lookup lk = lookup(caseTypeId, stageId);
    // Pre-check: reject if the id exists in the YAML base or in the existing overlay.
    // Read both the YAML base and the store overlay at lk.version (the registry-bound version),
    // NOT via resolver.resolveOne which uses caseType.version() and diverges post-3.4.1.
    boolean inYamlBase =
        lk.caseType.statusesFor(stageId).stream().anyMatch(s -> s.id().equals(statusId));
    boolean inOverlay =
        store.findOne(caseTypeId, lk.version, sentinel(stageId), statusId).isPresent();
    if (inYamlBase || inOverlay) {
      throw new StatusOptionsStore.DuplicateStatusException(
          "status '" + statusId + "' already exists on " + caseTypeId + " stage=" + stageId);
    }
    return store.append(
        caseTypeId, lk.version, sentinel(stageId), statusId, displayName, color, terminal);
  }

  /**
   * Rename an existing status' label / color. Mutate-class concerns ({@code terminal}, removal,
   * retarget) are rejected by the controller before reaching this method.
   *
   * <p>Semantics for the YAML base: a rename of a YAML-declared status writes a row into {@code
   * status_options} for the SAME id; the resolver's overlay logic then surfaces the new
   * displayName/color while preserving the YAML's relative position. This matches Decision 21's
   * "live edit" promise and keeps the deploy-time YAML intact (no in-place rewrite).
   *
   * @throws StatusAdminLookupFailure when {@code caseTypeId} / {@code stageId} / {@code statusId}
   *     is unknown (the latter mapped to {@code WKS-STG-013} by the controller)
   */
  public StatusDefinition rename(
      String caseTypeId, String stageId, String statusId, String displayName, String color) {
    Lookup lk = lookup(caseTypeId, stageId);
    Optional<StatusDefinition> resolved = resolver.resolveOne(lk.caseType, stageId, statusId);
    if (resolved.isEmpty()) {
      throw new StatusAdminLookupFailure(LookupKind.STATUS, statusId);
    }
    StatusDefinition base = resolved.get();
    String newDisplayName = displayName != null ? displayName : base.displayName();
    String newColor = color != null ? color : base.color().wire();
    // Upsert: if no row exists yet (YAML-declared status that has never been edited), insert one
    // at the end of the overlay so future reads project the rename. Append uses the YAML base's
    // ordering; ordinal here is best-effort tail-of-overlay.
    Optional<StatusDefinition> existingRow =
        store.findOne(caseTypeId, lk.version, sentinel(stageId), statusId);
    if (existingRow.isEmpty()) {
      return store.append(
          caseTypeId,
          lk.version,
          sentinel(stageId),
          statusId,
          newDisplayName,
          newColor,
          base.terminal());
    }
    return store
        .rename(caseTypeId, lk.version, sentinel(stageId), statusId, newDisplayName, newColor)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "rename returned empty after findOne returned present — inconsistent store"));
  }

  private Lookup lookup(String caseTypeId, String stageId) {
    Optional<CaseTypeConfig> ctOpt = reader.find(caseTypeId);
    if (ctOpt.isEmpty()) {
      throw new StatusAdminLookupFailure(LookupKind.CASE_TYPE, caseTypeId);
    }
    CaseTypeConfig ct = ctOpt.get();
    // For non-flat stage requests, verify the stage is declared on the case type.
    if (!StatusOptionsStore.FLAT_SENTINEL.equals(stageId)
        && !StatusOptionsResolver.isDeclaredStage(ct, stageId)) {
      throw new StatusAdminLookupFailure(LookupKind.STAGE, stageId);
    }
    int version =
        versionRegistry
            .currentVersion(caseTypeId)
            .orElse(ct.version()); // fall back to in-memory registry version (test-only paths)
    return new Lookup(ct, version);
  }

  private static String sentinel(String stageId) {
    return stageId == null || stageId.isBlank() ? StatusOptionsStore.FLAT_SENTINEL : stageId;
  }

  /** Read-side resolver delegate (exposed for the controller to drive admin GET). */
  public StatusOptionsResolver resolver() {
    return resolver;
  }

  // --- types ----------------------------------------------------------------

  public enum LookupKind {
    CASE_TYPE,
    STAGE,
    STATUS
  }

  /**
   * Surfaced as {@code WKS-STG-012} (CASE_TYPE / STAGE) or {@code WKS-STG-013} (STATUS) by the
   * controller — domain layer stays HTTP-free.
   */
  public static class StatusAdminLookupFailure extends RuntimeException {
    private final LookupKind kind;
    private final String missingId;

    public StatusAdminLookupFailure(LookupKind kind, String missingId) {
      super(kind.name().toLowerCase() + " '" + missingId + "' not found");
      this.kind = kind;
      this.missingId = missingId;
    }

    public LookupKind kind() {
      return kind;
    }

    public String missingId() {
      return missingId;
    }
  }

  public record ResolvedStatusSet(
      String stageId, String initialStatus, List<StatusDefinition> statuses) {
    public ResolvedStatusSet {
      statuses = List.copyOf(statuses);
    }
  }

  private record Lookup(CaseTypeConfig caseType, int version) {}
}
