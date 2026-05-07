package com.wkspower.platform.api.mapper;

import com.wkspower.platform.api.dto.response.CaseDto;
import com.wkspower.platform.api.dto.response.CaseSummaryDto;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto.FieldView;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto.FormDefinitionView;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto.OptionView;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto.StageDefinitionView;
import com.wkspower.platform.api.dto.response.StageView;
import com.wkspower.platform.api.dto.response.StatusView;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.model.Stage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain-model → wire-DTO mapping for the case CRUD endpoints. Manual mapping (no MapStruct) per
 * Story 1.4 precedent.
 */
public final class CaseDtoMapper {

  private CaseDtoMapper() {
    // utility
  }

  /**
   * Story 2.3 entry point — kept for callers (e.g. write-paths and tests) that don't yet have the
   * stage history loaded. Projects an empty stage list, which is correct for zero-stage CaseTypes
   * and a safe default for write-paths that re-fetch the case immediately afterward. New read-paths
   * (Story 3.3 onwards) should use {@link #toDto(Case, CaseTypeConfig, List)} so the timeline
   * renders without a second round-trip.
   */
  public static CaseDto toDto(Case domain, CaseTypeConfig caseType) {
    return toDto(domain, caseType, List.of());
  }

  /**
   * Story 3.3 — full read-model projection. {@code history} is the output of {@code
   * StageRepository.loadHistory(caseId)}; the mapper joins each row to its declared {@code
   * StageDefinition} (via {@code stageId}) so the wire {@link StageView} carries the bound
   * CaseType@version display name + ordinal. Skipped stages stay in the list at their declared
   * ordinal; the list is sorted ASC by ordinal — the timeline never omits or reorders stages.
   *
   * <p>Zero-row history (zero-stage CaseType) maps to {@code stages = []} — no {@code if
   * (history.isEmpty())} branch is needed, the empty-list mapping is the empty-stage path (Decision
   * 19's "stage-less paths must remain unbranched in code").
   */
  public static CaseDto toDto(Case domain, CaseTypeConfig caseType, List<Stage> history) {
    Map<String, StageDefinition> defsById = new HashMap<>();
    for (StageDefinition def : caseType.stages()) {
      defsById.put(def.id(), def);
    }
    List<StageView> stageViews =
        history.stream()
            .sorted((a, b) -> Integer.compare(a.ordinal(), b.ordinal()))
            .map(s -> toStageView(s, defsById.get(s.stageId())))
            .toList();
    return new CaseDto(
        domain.id(),
        domain.caseTypeId(),
        domain.caseTypeVersion(),
        domain.status(),
        domain.assignee(),
        domain.data(),
        domain.processInstanceId(),
        0, // documentCount — Epic 3 fills this; AC5 freezes the field at 0 in Phase 0.
        domain.createdAt(),
        domain.createdBy(),
        domain.updatedAt(),
        domain.version(),
        toCaseTypeView(caseType),
        // Story 3.2 AC5 — null on zero-stage CaseTypes; populated from the cases table cache cols
        // (Story 3.1) for staged cases.
        domain.currentStageId(),
        domain.currentStageOrdinal(),
        // Story 3.3 — full stage history projection (empty list for zero-stage CaseTypes).
        stageViews,
        // Story 3.6 AC8 — resolved status set for the active stage (stage-scoped if declared, flat
        // fallback otherwise). Empty only when both stage-scoped and flat sets are empty — should
        // be impossible after Story 3.2's default. Decision 19's unbranched-paths invariant: this
        // call site does not branch on stage presence; statusesFor() handles the absence in one
        // place.
        toAvailableStatuses(caseType, domain.currentStageId()));
  }

  /**
   * Story 3.6 AC8 — resolve the available status list for the case's current stage. Pure read; no
   * I/O. The {@code currentStageId} can be {@code null} (zero-stage CaseType, or all stages
   * completed) — in that case {@code statusesFor(null)} returns the flat set.
   */
  static List<StatusView> toAvailableStatuses(CaseTypeConfig caseType, String currentStageId) {
    List<StatusDefinition> resolved = caseType.statusesFor(currentStageId);
    List<StatusView> out = new java.util.ArrayList<>(resolved.size());
    for (int i = 0; i < resolved.size(); i++) {
      StatusDefinition s = resolved.get(i);
      out.add(
          new StatusView(
              s.id(),
              s.displayName(),
              s.color() == null ? null : s.color().wire(),
              s.terminal(),
              i));
    }
    return out;
  }

  static StageView toStageView(Stage stage, StageDefinition def) {
    // The history row's stageId must always match a declared stage on the bound CaseType@version.
    // A null def here would mean schema drift between the case and its bound CaseType — a defensive
    // fallback (Title-cased stageId) is safer than throwing inside a read-only projection.
    String displayName = def != null ? def.displayName() : titleCase(stage.stageId());
    return new StageView(
        stage.stageId(),
        displayName,
        stage.ordinal(),
        stage.state().name(),
        stage.enteredAt(),
        stage.exitedAt(),
        stage.source(),
        stage.sourceRef());
  }

  private static String titleCase(String id) {
    if (id == null || id.isBlank()) return id;
    StringBuilder sb = new StringBuilder(id.length());
    boolean upper = true;
    for (char c : id.toCharArray()) {
      if (c == '-' || c == '_') {
        sb.append(' ');
        upper = true;
      } else if (upper) {
        sb.append(Character.toUpperCase(c));
        upper = false;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public static CaseSummaryDto toSummaryDto(CaseSummary summary) {
    return new CaseSummaryDto(
        summary.id(),
        summary.caseTypeId(),
        summary.status(),
        summary.assignee(),
        summary.createdAt(),
        summary.updatedAt(),
        summary.fields());
  }

  public static CaseTypeViewDto toCaseTypeView(CaseTypeConfig caseType) {
    List<StageDefinitionView> stages =
        caseType.stages().stream()
            .map(s -> new StageDefinitionView(s.id(), s.displayName(), s.ordinal()))
            .toList();
    // Story 5.2 — map form definitions to wire DTOs.
    List<FormDefinitionView> forms =
        caseType.forms().stream().map(CaseDtoMapper::toFormDefinitionView).toList();
    return new CaseTypeViewDto(
        caseType.id(),
        caseType.displayName(),
        caseType.version(),
        caseType.fields().stream().map(CaseDtoMapper::toFieldView).toList(),
        caseType.statuses(),
        caseType.listColumns(),
        stages,
        forms);
  }

  /**
   * Story 5.2 — map one {@link FormDefinition} to its wire DTO. Fields within the form definition
   * are mapped using the same {@link #toFieldView} helper used for case-type-level fields.
   */
  static FormDefinitionView toFormDefinitionView(FormDefinition form) {
    List<FieldView> fieldViews = form.fields().stream().map(CaseDtoMapper::toFieldView).toList();
    return new FormDefinitionView(
        form.id(), form.topology(), form.dataModel(), form.rendering(), fieldViews);
  }

  static FieldView toFieldView(FieldDefinition f) {
    FieldDefinition.TypeSlots s = f.slots();
    List<OptionView> options =
        f.options().stream().map(o -> new OptionView(o.label(), o.value())).toList();
    return new FieldView(
        f.id(),
        f.displayName(),
        f.type().wire(),
        f.required(),
        f.requiredOnCreate(),
        f.order(),
        options,
        s == null ? null : s.minLength(),
        s == null ? null : s.maxLength(),
        s == null ? null : s.min(),
        s == null ? null : s.max(),
        s == null ? null : s.step(),
        s == null ? null : s.dateMin(),
        s == null ? null : s.dateMax(),
        s == null ? null : s.maxBytes(),
        s == null || s.allowedMimeTypes() == null ? List.of() : s.allowedMimeTypes());
  }
}
