package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.port.StatusOptionsStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * Story 3.7 — resolver that overlays {@link StatusOptionsStore} append-class deltas on top of the
 * frozen-on-version YAML status base produced by {@link CaseTypeConfig#statusesFor(String)}.
 *
 * <p>Live-propagation strategy: <strong>(a) re-read on every call</strong>. No cache layer. Q1
 * locks "fully live" — existing mid-flight cases see the appended status on next read; the
 * trade-off is one extra DB round-trip per status read. Acceptable for Phase-0 traffic per the
 * Story 3.7 brief; a short-TTL cache (option b) is a defensible Phase-1 optimisation if observed
 * load demands it.
 *
 * <p>Pure domain — no Spring, no JPA. The resolver is constructed directly by callers (admin
 * controller wiring) and given the {@link StatusOptionsStore} port.
 */
public class StatusOptionsResolver {

  private final StatusOptionsStore store;

  public StatusOptionsResolver(StatusOptionsStore store) {
    this.store = store;
  }

  /**
   * Resolve the effective status list for {@code (caseType, stageId)} at the given bound version.
   * The base list comes from the frozen-on-version YAML; appended/renamed rows from {@code
   * status_options} for the SAME version overlay onto it:
   *
   * <ul>
   *   <li>matching {@code statusId} → renamed entry replaces the YAML base
   *   <li>new {@code statusId} → appended after the YAML base, in ordinal order
   * </ul>
   *
   * <p>{@code stageId} may be {@code null} for a zero-stage CaseType or a case with no active stage
   * — the flat case-type-level set is returned (and the {@code status_options} read uses the {@link
   * StatusOptionsStore#FLAT_SENTINEL}).
   */
  public List<StatusDefinition> resolve(CaseTypeConfig caseType, String stageId) {
    List<StatusDefinition> base = caseType.statusesFor(stageId);
    String storeStageId = stageId == null ? StatusOptionsStore.FLAT_SENTINEL : stageId;
    List<StatusDefinition> overlay = store.listFor(caseType.id(), caseType.version(), storeStageId);
    if (overlay.isEmpty()) {
      return base;
    }
    // Preserve declared order: keep YAML base ordering, replace by id where overlay has a match,
    // append new ids in their persisted ordinal order at the tail.
    LinkedHashMap<String, StatusDefinition> byId = new LinkedHashMap<>();
    for (StatusDefinition s : base) {
      byId.put(s.id(), s);
    }
    List<StatusDefinition> tail = new ArrayList<>();
    for (StatusDefinition s : overlay) {
      if (byId.containsKey(s.id())) {
        byId.put(s.id(), s); // rename overlay
      } else {
        tail.add(s); // append
      }
    }
    List<StatusDefinition> result = new ArrayList<>(byId.size() + tail.size());
    result.addAll(byId.values());
    result.addAll(tail);
    return List.copyOf(result);
  }

  /**
   * Convenience — returns the resolved {@link StatusDefinition} matching {@code statusId} or empty.
   * Used by the admin PATCH path to verify the target id exists somewhere (YAML base OR overlay)
   * before writing the rename.
   */
  public Optional<StatusDefinition> resolveOne(
      CaseTypeConfig caseType, String stageId, String statusId) {
    return resolve(caseType, stageId).stream().filter(s -> s.id().equals(statusId)).findFirst();
  }

  /**
   * Returns true when {@code stageId} is declared on {@code caseType}. Convenience for the admin
   * controller's 404 path. {@code null} is rejected (admin path requires explicit stage selection).
   */
  public static boolean isDeclaredStage(CaseTypeConfig caseType, String stageId) {
    if (stageId == null) {
      return false;
    }
    return caseType.stages().stream().map(StageDefinition::id).anyMatch(stageId::equals);
  }
}
