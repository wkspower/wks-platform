package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldOption;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Story 3.11 AC1 — minimal best-effort projection from {@link RawCaseTypeConfig} to {@link
 * CaseTypeConfig}, used by the lenient prior-YAML re-parse path on the blast-radius diff. Skips
 * semantic validation: entries with missing/blank ids or unknown enum values are dropped silently
 * rather than raising errors. This is intentional — diffing a schema-drifted prior version against
 * a strictly-validated candidate must not surface validator findings against the obsolete schema
 * (those findings ran when the prior was first deployed and would be noise here).
 *
 * <p>Strict path callers continue to use {@link ConfigValidator#validate} which performs full
 * semantic checks AND projection in one pass. This helper is the projection-only sibling — never
 * call it from the candidate (next) YAML path.
 */
final class CaseTypeConfigProjection {

  private CaseTypeConfigProjection() {
    // utility
  }

  /**
   * Project a parsed {@link RawCaseTypeConfig} to a {@link CaseTypeConfig} for diff purposes.
   * Returns {@link Optional#empty()} when the raw is null or lacks required top-level fields the
   * diff cannot proceed without ({@code id}, {@code displayName}, {@code version}). All collection
   * accessors on the returned config are non-null, matching the diff's structural expectations.
   */
  static Optional<CaseTypeConfig> project(RawCaseTypeConfig raw) {
    if (raw == null
        || raw.id() == null
        || raw.id().isBlank()
        || raw.displayName() == null
        || raw.displayName().isBlank()
        || raw.version() == null) {
      return Optional.empty();
    }

    List<FieldDefinition> fields = projectFields(raw.fields());
    List<StatusDefinition> statuses = projectStatuses(raw.statuses());
    List<RoleDefinition> roles = projectRoles(raw.roles());
    List<String> listColumns =
        raw.listColumns() == null ? List.of() : List.copyOf(raw.listColumns());
    List<StageDefinition> stages = projectStages(raw.stages());
    List<FormDefinition> forms = projectForms(raw.forms());
    WorkflowRef workflow = raw.workflow() == null ? null : new WorkflowRef(raw.workflow().bpmn());

    return Optional.of(
        new CaseTypeConfig(
            raw.id(),
            raw.displayName(),
            raw.version(),
            raw.description(),
            workflow,
            fields,
            statuses,
            listColumns,
            roles,
            stages,
            forms));
  }

  private static List<FieldDefinition> projectFields(List<RawCaseTypeConfig.RawField> raws) {
    if (raws == null || raws.isEmpty()) {
      return List.of();
    }
    List<FieldDefinition> out = new ArrayList<>(raws.size());
    for (int i = 0; i < raws.size(); i++) {
      RawCaseTypeConfig.RawField f = raws.get(i);
      if (f == null || f.id() == null || f.id().isBlank()) {
        continue;
      }
      Optional<FieldType> type = FieldType.fromWire(f.type());
      if (type.isEmpty()) {
        // Unknown type — skip rather than guess; diff cannot meaningfully classify a field with
        // no known type.
        continue;
      }
      boolean required = Boolean.TRUE.equals(f.required());
      boolean requiredOnCreate = f.requiredOnCreate() == null ? required : f.requiredOnCreate();
      int order = f.order() == null ? i + 1 : f.order();
      List<FieldOption> opts = List.of();
      if (f.options() != null) {
        List<FieldOption> tmp = new ArrayList<>(f.options().size());
        for (RawCaseTypeConfig.RawOption o : f.options()) {
          if (o == null || o.value() == null) {
            continue;
          }
          String label = o.label() == null ? o.value() : o.label();
          tmp.add(new FieldOption(label, o.value()));
        }
        opts = List.copyOf(tmp);
      }
      String displayName = f.displayName() == null ? f.id() : f.displayName();
      FieldDefinition.TypeSlots slots =
          new FieldDefinition.TypeSlots(
              f.minLength(),
              f.maxLength(),
              f.min(),
              f.max(),
              f.step(),
              f.dateMin(),
              f.dateMax(),
              f.maxBytes(),
              f.allowedMimeTypes() == null ? List.of() : List.copyOf(f.allowedMimeTypes()));
      out.add(
          new FieldDefinition(
              f.id(), displayName, type.get(), required, requiredOnCreate, order, opts, slots));
    }
    return List.copyOf(out);
  }

  private static List<StatusDefinition> projectStatuses(List<RawCaseTypeConfig.RawStatus> raws) {
    if (raws == null || raws.isEmpty()) {
      return List.of();
    }
    List<StatusDefinition> out = new ArrayList<>(raws.size());
    for (RawCaseTypeConfig.RawStatus s : raws) {
      if (s == null || s.id() == null || s.id().isBlank()) {
        continue;
      }
      StatusColor color = parseColor(s.color());
      if (color == null) {
        // Unknown color — diff doesn't read color, but the StatusDefinition record requires
        // non-null. Default to ZINC (neutral) for diff-only purposes.
        color = StatusColor.ZINC;
      }
      String displayName = s.displayName() == null ? s.id() : s.displayName();
      boolean terminal = Boolean.TRUE.equals(s.terminal());
      out.add(new StatusDefinition(s.id(), displayName, color, terminal));
    }
    return List.copyOf(out);
  }

  private static StatusColor parseColor(String wire) {
    if (wire == null) {
      return null;
    }
    for (StatusColor c : StatusColor.values()) {
      if (c.name().equalsIgnoreCase(wire)) {
        return c;
      }
    }
    return null;
  }

  private static List<RoleDefinition> projectRoles(List<RawCaseTypeConfig.RawRole> raws) {
    if (raws == null || raws.isEmpty()) {
      return List.of();
    }
    List<RoleDefinition> out = new ArrayList<>(raws.size());
    for (RawCaseTypeConfig.RawRole r : raws) {
      if (r == null || r.name() == null || r.name().isBlank()) {
        continue;
      }
      List<Permission> perms = List.of();
      if (r.permissions() != null) {
        List<Permission> tmp = new ArrayList<>(r.permissions().size());
        for (String p : r.permissions()) {
          Permission.fromWire(p).ifPresent(tmp::add);
        }
        perms = List.copyOf(tmp);
      }
      out.add(new RoleDefinition(r.name(), perms));
    }
    return List.copyOf(out);
  }

  private static List<StageDefinition> projectStages(List<RawCaseTypeConfig.RawStage> raws) {
    if (raws == null || raws.isEmpty()) {
      return List.of();
    }
    List<StageDefinition> out = new ArrayList<>(raws.size());
    int ordinal = 0;
    for (RawCaseTypeConfig.RawStage s : raws) {
      if (s == null || s.id() == null || s.id().isBlank()) {
        continue;
      }
      String displayName =
          s.displayName() == null || s.displayName().isBlank() ? s.id() : s.displayName();
      List<StatusDefinition> stageStatuses = projectStatuses(s.statuses());
      // null = not declared (flat fallback); empty list = declared empty (rare). Preserve null
      // when raw.statuses() was null so diff sees the same "absent" signal as the strict path.
      List<StatusDefinition> effective = s.statuses() == null ? null : stageStatuses;
      Optional<String> initial =
          s.initialStatus() == null || s.initialStatus().isBlank()
              ? Optional.empty()
              : Optional.of(s.initialStatus());
      out.add(
          new StageDefinition(s.id(), displayName, ordinal++, effective, initial, s.archetype()));
    }
    return List.copyOf(out);
  }

  private static List<FormDefinition> projectForms(RawFormConfig forms) {
    if (forms == null || forms.definitions() == null || forms.definitions().isEmpty()) {
      return List.of();
    }
    List<FormDefinition> out = new ArrayList<>(forms.definitions().size());
    for (RawFormDefinition rf : forms.definitions()) {
      if (rf == null || rf.id() == null || rf.id().isBlank()) {
        continue;
      }
      try {
        out.add(FormDefinitionMapper.toDomain(rf));
      } catch (RuntimeException ignored) {
        // Best-effort projection — if the form's internal shape is too drifted to map, skip.
        // Diff classifier only consults form ids; missing forms count as removals and the operator
        // can investigate the WARN trail.
      }
    }
    return List.copyOf(out);
  }
}
