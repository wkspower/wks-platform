package com.wkspower.platform.domain.config.diff;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.MappingChangeClass;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.infrastructure.config.MappingDiff;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Story 3.8 — pure-function blast-radius classifier for the whole-CaseType diff surface. Given a
 * prior and a candidate {@link CaseTypeConfig}, classifies every detected change as either
 * append-class (safe, no version bump required) or mutate-class (requires {@code bumpVersion=true}).
 *
 * <p><b>Phase-0 scope (Story 3.8):</b> the following change kinds are intentionally out-of-scope
 * and treated as append-class to avoid false-positive mutate classifications:
 *
 * <ul>
 *   <li>Field option-list edits ({@code options[]} changes on select-type fields)
 *   <li>Role-permissions edits ({@code permissions[]} changes on existing roles)
 *   <li>{@code listColumns} reordering
 *   <li>{@code displayName}-only changes at any level
 * </ul>
 *
 * Phase-1 (Story 3.9+) may tighten any of these. Each omission is documented with a {@code //
 * Phase-0 scope} comment below.
 *
 * <p><b>Stage-list semantics (AC1 boundary):</b>
 *
 * <ul>
 *   <li>If {@code next.stages} strictly extends {@code prev.stages} at the tail — i.e. {@code
 *       next.size() >= prev.size()} AND {@code next.subList(0, prev.size())} has the same stage ids
 *       in the same order as {@code prev.stages} — all extra stages emit {@link
 *       DeltaKind#STAGE_APPENDED} (append-class).
 *   <li>Otherwise → emit {@link DeltaKind#STAGE_INSERTED_MIDDLE} or {@link
 *       DeltaKind#STAGE_REORDERED} per detected pattern (mutate-class).
 * </ul>
 *
 * <p><b>Pure static — no Spring beans, no I/O.</b> Mirrors {@link MappingDiff} exactly.
 */
public final class CaseTypeDiff {

  private CaseTypeDiff() {}

  /**
   * Classify every difference between {@code prev} and {@code next} into a {@link
   * BlastRadiusReport}.
   *
   * @param prev the prior validated {@link CaseTypeConfig} (loaded from {@code
   *     case_type_versions.definition_yaml} via the version registry)
   * @param next the candidate {@link CaseTypeConfig} being deployed
   * @param prevMapping the prior {@link MappingDefinition}; use {@link MappingDefinition#empty()}
   *     when no prior mapping exists
   * @param nextMapping the candidate {@link MappingDefinition}; use {@link
   *     MappingDefinition#empty()} for zero-attachment deploys
   * @return a {@link BlastRadiusReport} containing two lists: {@code appendDeltas} and {@code
   *     mutateDeltas}
   */
  public static BlastRadiusReport classify(
      CaseTypeConfig prev,
      CaseTypeConfig next,
      MappingDefinition prevMapping,
      MappingDefinition nextMapping) {
    Objects.requireNonNull(prev, "prev");
    Objects.requireNonNull(next, "next");
    Objects.requireNonNull(prevMapping, "prevMapping");
    Objects.requireNonNull(nextMapping, "nextMapping");

    List<Delta> appendDeltas = new ArrayList<>();
    List<Delta> mutateDeltas = new ArrayList<>();

    diffStatuses(prev, next, appendDeltas, mutateDeltas);
    diffFields(prev, next, appendDeltas, mutateDeltas);
    diffStages(prev, next, appendDeltas, mutateDeltas);
    diffRoles(prev, next, appendDeltas, mutateDeltas);
    diffForms(prev, next, appendDeltas, mutateDeltas);
    diffMapping(prevMapping, nextMapping, appendDeltas, mutateDeltas);

    return new BlastRadiusReport(List.copyOf(appendDeltas), List.copyOf(mutateDeltas));
  }

  // -------------------------------------------------------------------------
  // Status diff
  // -------------------------------------------------------------------------

  /**
   * Detects status changes across all scopes (case-type-level + every stage-scoped set).
   *
   * <p>For each status id seen in prev:
   *
   * <ul>
   *   <li>If absent in next anywhere → STATUS_REMOVED (mutate)
   *   <li>If present but in a different stage scope → STATUS_RETARGETED (mutate)
   *   <li>If {@code terminal} flag changed (same scope) → STATUS_TERMINAL_FLIP (mutate)
   * </ul>
   *
   * For each status id seen in next but not in prev → STATUS_ADDED (append).
   *
   * <p>Phase-0 scope: {@code displayName} and {@code color} changes are not classified.
   */
  private static void diffStatuses(
      CaseTypeConfig prev,
      CaseTypeConfig next,
      List<Delta> appendDeltas,
      List<Delta> mutateDeltas) {

    // Build flat maps: statusId → (stageId or null for case-type-level, terminal flag)
    Map<String, StatusEntry> prevStatuses = collectAllStatuses(prev);
    Map<String, StatusEntry> nextStatuses = collectAllStatuses(next);

    // Check for removals, retargets, and terminal flips
    for (Map.Entry<String, StatusEntry> e : prevStatuses.entrySet()) {
      String statusId = e.getKey();
      StatusEntry prevEntry = e.getValue();
      StatusEntry nextEntry = nextStatuses.get(statusId);

      if (nextEntry == null) {
        // STATUS_REMOVED
        String path = statusPath(statusId, prevEntry.stageId());
        mutateDeltas.add(
            new Delta(
                DeltaKind.STATUS_REMOVED,
                path,
                "status '" + statusId + "' was removed"));
      } else {
        // Check retarget across stages
        boolean prevScoped = prevEntry.stageId() != null;
        boolean nextScoped = nextEntry.stageId() != null;
        boolean sameScope =
            Objects.equals(prevEntry.stageId(), nextEntry.stageId());

        if (!sameScope) {
          String fromScope = prevScoped ? "stage '" + prevEntry.stageId() + "'" : "case-type level";
          String toScope = nextScoped ? "stage '" + nextEntry.stageId() + "'" : "case-type level";
          String path = statusPath(statusId, prevEntry.stageId());
          mutateDeltas.add(
              new Delta(
                  DeltaKind.STATUS_RETARGETED,
                  path,
                  "status '"
                      + statusId
                      + "' was retargeted from "
                      + fromScope
                      + " to "
                      + toScope));
        } else if (prevEntry.terminal() != nextEntry.terminal()) {
          // STATUS_TERMINAL_FLIP (same scope)
          String path = statusPath(statusId, prevEntry.stageId()) + "/terminal";
          mutateDeltas.add(
              new Delta(
                  DeltaKind.STATUS_TERMINAL_FLIP,
                  path,
                  "status '"
                      + statusId
                      + "' terminal flag changed from "
                      + prevEntry.terminal()
                      + " to "
                      + nextEntry.terminal()));
        }
        // Phase-0 scope: displayName + color changes not classified
      }
    }

    // Check for new statuses (append)
    for (Map.Entry<String, StatusEntry> e : nextStatuses.entrySet()) {
      String statusId = e.getKey();
      if (!prevStatuses.containsKey(statusId)) {
        String path = statusPath(statusId, e.getValue().stageId());
        appendDeltas.add(
            new Delta(DeltaKind.STATUS_ADDED, path, "status '" + statusId + "' was added"));
      }
    }
  }

  /** Collect all statuses across case-type level and all stage-scoped sets. */
  private static Map<String, StatusEntry> collectAllStatuses(CaseTypeConfig config) {
    Map<String, StatusEntry> result = new HashMap<>();

    // Case-type-level statuses (stageId = null)
    for (StatusDefinition s : config.statuses()) {
      result.put(s.id(), new StatusEntry(null, s.terminal()));
    }

    // Stage-scoped statuses
    for (StageDefinition stage : config.stages()) {
      if (stage.statuses() != null) {
        for (StatusDefinition s : stage.statuses()) {
          // Stage-scoped wins over case-type-level for the same id
          result.put(s.id(), new StatusEntry(stage.id(), s.terminal()));
        }
      }
    }

    return result;
  }

  private record StatusEntry(String stageId, boolean terminal) {}

  private static String statusPath(String statusId, String stageId) {
    if (stageId != null) {
      return "/stages/" + stageId + "/statuses/" + statusId + "/id";
    }
    return "/statuses/" + statusId + "/id";
  }

  // -------------------------------------------------------------------------
  // Field diff
  // -------------------------------------------------------------------------

  /**
   * Detects field changes.
   *
   * <ul>
   *   <li>Field removed → FIELD_REMOVED (mutate)
   *   <li>Field type changed → FIELD_RETYPED (mutate)
   *   <li>Field required-ness changed (same id) → FIELD_REQUIRED_FLIPPED (mutate)
   *   <li>New field added → FIELD_ADDED (append, regardless of required value)
   * </ul>
   *
   * <p>Phase-0 scope: {@code displayName}, {@code order}, {@code options[]}, and {@code
   * requiredOnCreate} changes are not classified.
   */
  private static void diffFields(
      CaseTypeConfig prev,
      CaseTypeConfig next,
      List<Delta> appendDeltas,
      List<Delta> mutateDeltas) {

    Map<String, FieldDefinition> prevFields = indexById(prev.fields(), FieldDefinition::id);
    Map<String, FieldDefinition> nextFields = indexById(next.fields(), FieldDefinition::id);

    for (Map.Entry<String, FieldDefinition> e : prevFields.entrySet()) {
      String fieldId = e.getKey();
      FieldDefinition prevField = e.getValue();
      FieldDefinition nextField = nextFields.get(fieldId);

      if (nextField == null) {
        // FIELD_REMOVED
        mutateDeltas.add(
            new Delta(
                DeltaKind.FIELD_REMOVED,
                "/fields/" + fieldId + "/id",
                "field '" + fieldId + "' was removed"));
      } else {
        // FIELD_RETYPED
        if (prevField.type() != nextField.type()) {
          mutateDeltas.add(
              new Delta(
                  DeltaKind.FIELD_RETYPED,
                  "/fields/" + fieldId + "/type",
                  "field '"
                      + fieldId
                      + "' type changed from "
                      + prevField.type()
                      + " to "
                      + nextField.type()));
        }
        // FIELD_REQUIRED_FLIPPED
        if (prevField.required() != nextField.required()) {
          mutateDeltas.add(
              new Delta(
                  DeltaKind.FIELD_REQUIRED_FLIPPED,
                  "/fields/" + fieldId + "/required",
                  "field '"
                      + fieldId
                      + "' required changed from "
                      + prevField.required()
                      + " to "
                      + nextField.required()));
        }
        // Phase-0 scope: displayName, order, options[], requiredOnCreate not classified
      }
    }

    // New fields
    for (String fieldId : nextFields.keySet()) {
      if (!prevFields.containsKey(fieldId)) {
        appendDeltas.add(
            new Delta(
                DeltaKind.FIELD_ADDED,
                "/fields/" + fieldId + "/id",
                "field '" + fieldId + "' was added"));
      }
    }
  }

  // -------------------------------------------------------------------------
  // Stage diff
  // -------------------------------------------------------------------------

  /**
   * Detects stage-list changes.
   *
   * <p>Rule: if {@code next.stages} strictly extends {@code prev.stages} at the tail (all prev
   * stage ids appear in the same order at the head of next), all extra stages emit {@link
   * DeltaKind#STAGE_APPENDED} (append). Otherwise, emit mutate-class deltas per detected pattern:
   *
   * <ul>
   *   <li>New stage whose position shifts an existing stage → {@link DeltaKind#STAGE_INSERTED_MIDDLE}
   *   <li>Existing stages reordered or removed → {@link DeltaKind#STAGE_REORDERED}
   * </ul>
   *
   * <p>Phase-0 scope: {@code displayName} changes on existing stages are not classified.
   */
  private static void diffStages(
      CaseTypeConfig prev,
      CaseTypeConfig next,
      List<Delta> appendDeltas,
      List<Delta> mutateDeltas) {

    List<StageDefinition> prevStages = prev.stages();
    List<StageDefinition> nextStages = next.stages();

    if (prevStages.isEmpty() && nextStages.isEmpty()) {
      return;
    }

    // Collect ids for comparison
    List<String> prevIds = prevStages.stream().map(StageDefinition::id).toList();
    List<String> nextIds = nextStages.stream().map(StageDefinition::id).toList();

    // Strict tail-extension check
    boolean isTailExtension = isTailExtension(prevIds, nextIds);

    if (isTailExtension) {
      // All extra stages at the tail are append-class
      for (int i = prevIds.size(); i < nextIds.size(); i++) {
        appendDeltas.add(
            new Delta(
                DeltaKind.STAGE_APPENDED,
                "/stages/" + nextIds.get(i) + "/id",
                "stage '" + nextIds.get(i) + "' was appended at position " + i));
      }
      return;
    }

    // Not a tail extension — classify the non-trivial changes
    Set<String> prevIdSet = new HashSet<>(prevIds);
    Set<String> nextIdSet = new HashSet<>(nextIds);

    // Stages removed from prev
    for (String id : prevIds) {
      if (!nextIdSet.contains(id)) {
        mutateDeltas.add(
            new Delta(
                DeltaKind.STAGE_REORDERED,
                "/stages/" + id + "/id",
                "stage '" + id + "' was removed"));
      }
    }

    // New stages inserted (not at tail)
    for (int i = 0; i < nextIds.size(); i++) {
      String id = nextIds.get(i);
      if (!prevIdSet.contains(id)) {
        // Determine if this is a middle insertion or tail
        // If i < prevIds.size() it is definitely a middle insertion
        if (i < prevIds.size()) {
          mutateDeltas.add(
              new Delta(
                  DeltaKind.STAGE_INSERTED_MIDDLE,
                  "/stages/" + id + "/id",
                  "stage '"
                      + id
                      + "' was inserted at position "
                      + i
                      + " (middle insertion shifts existing stage ordinals)"));
        } else {
          // Logically at the tail, but we already know it's not a pure tail extension
          // because the prefix didn't match — still append-class for this particular stage
          appendDeltas.add(
              new Delta(
                  DeltaKind.STAGE_APPENDED,
                  "/stages/" + id + "/id",
                  "stage '" + id + "' was appended"));
        }
      }
    }

    // Check if existing stages (present in both) were reordered
    List<String> commonPrevOrder =
        prevIds.stream().filter(nextIdSet::contains).toList();
    List<String> commonNextOrder =
        nextIds.stream().filter(prevIdSet::contains).toList();

    if (!commonPrevOrder.equals(commonNextOrder)) {
      // The relative order of existing stages changed
      for (String id : commonPrevOrder) {
        int prevOrd = prevIds.indexOf(id);
        int nextOrd = nextIds.indexOf(id);
        if (prevOrd != nextOrd) {
          mutateDeltas.add(
              new Delta(
                  DeltaKind.STAGE_REORDERED,
                  "/stages/" + id + "/id",
                  "stage '"
                      + id
                      + "' ordinal changed from "
                      + prevOrd
                      + " to "
                      + nextOrd));
        }
      }
    }
  }

  /**
   * Returns {@code true} iff {@code next} starts with all elements of {@code prev} in the same
   * order (allowing {@code next} to have additional elements at the tail).
   */
  private static boolean isTailExtension(List<String> prev, List<String> next) {
    if (next.size() < prev.size()) {
      return false;
    }
    for (int i = 0; i < prev.size(); i++) {
      if (!prev.get(i).equals(next.get(i))) {
        return false;
      }
    }
    return true;
  }

  // -------------------------------------------------------------------------
  // Role diff
  // -------------------------------------------------------------------------

  /**
   * Detects role additions.
   *
   * <ul>
   *   <li>New role → ROLE_ADDED (append)
   * </ul>
   *
   * <p>Phase-0 scope: existing role permission changes and role removal are not classified as mutate
   * (tracked for Phase-1).
   */
  private static void diffRoles(
      CaseTypeConfig prev,
      CaseTypeConfig next,
      List<Delta> appendDeltas,
      List<Delta> mutateDeltas) {

    Set<String> prevRoles = new HashSet<>();
    for (RoleDefinition r : prev.roles()) {
      prevRoles.add(r.name());
    }

    for (RoleDefinition r : next.roles()) {
      if (!prevRoles.contains(r.name())) {
        appendDeltas.add(
            new Delta(
                DeltaKind.ROLE_ADDED,
                "/roles/" + r.name() + "/name",
                "role '" + r.name() + "' was added"));
      }
    }
    // Phase-0 scope: role removal and permission changes not classified
  }

  // -------------------------------------------------------------------------
  // Form diff
  // -------------------------------------------------------------------------

  /**
   * Detects form additions.
   *
   * <ul>
   *   <li>New form → FORM_ADDED (append)
   * </ul>
   *
   * <p>Phase-0 scope: form removal and content changes are not classified as mutate.
   */
  private static void diffForms(
      CaseTypeConfig prev,
      CaseTypeConfig next,
      List<Delta> appendDeltas,
      List<Delta> mutateDeltas) {

    Set<String> prevForms = new HashSet<>();
    for (FormDefinition f : prev.forms()) {
      prevForms.add(f.id());
    }

    for (FormDefinition f : next.forms()) {
      if (!prevForms.contains(f.id())) {
        appendDeltas.add(
            new Delta(
                DeltaKind.FORM_ADDED,
                "/forms/" + f.id() + "/id",
                "form '" + f.id() + "' was added"));
      }
    }
    // Phase-0 scope: form removal and changes not classified
  }

  // -------------------------------------------------------------------------
  // Mapping diff (delegate to MappingDiff)
  // -------------------------------------------------------------------------

  /**
   * Delegates to {@link MappingDiff#classify} and folds the result into the report.
   *
   * <ul>
   *   <li>{@code MUTATE_CLASS} → adds a single {@link DeltaKind#MAPPING} delta to mutate list
   *   <li>{@code APPEND_CLASS} with actual change → adds a {@link DeltaKind#MAPPING_APPEND} delta
   *       to append list
   *   <li>Empty-to-empty → no delta emitted
   * </ul>
   */
  private static void diffMapping(
      MappingDefinition prev,
      MappingDefinition next,
      List<Delta> appendDeltas,
      List<Delta> mutateDeltas) {

    MappingChangeClass result = MappingDiff.classify(prev, next);

    if (result == MappingChangeClass.MUTATE_CLASS) {
      mutateDeltas.add(
          new Delta(
              DeltaKind.MAPPING,
              "/attachments",
              "BPMN mapping change requires version bump (MappingDiff returned MUTATE_CLASS)"));
    } else if (result == MappingChangeClass.APPEND_CLASS) {
      // Only emit a delta if there was an actual change (i.e. next is different from prev)
      if (!prev.attachments().equals(next.attachments())) {
        appendDeltas.add(
            new Delta(
                DeltaKind.MAPPING_APPEND,
                "/attachments",
                "BPMN mapping additive change (MappingDiff returned APPEND_CLASS)"));
      }
    }
  }

  // -------------------------------------------------------------------------
  // Utilities
  // -------------------------------------------------------------------------

  @FunctionalInterface
  private interface IdExtractor<T> {
    String id(T item);
  }

  private static <T> Map<String, T> indexById(List<T> items, IdExtractor<T> extractor) {
    Map<String, T> result = new HashMap<>();
    for (T item : items) {
      result.put(extractor.id(item), item);
    }
    return result;
  }
}
