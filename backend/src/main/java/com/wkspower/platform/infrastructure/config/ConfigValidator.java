package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.config.CaseTypeLimits;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldOption;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Collect-all validator for case-type YAML. Never short-circuits on first error — reviewers WILL
 * grep for early {@code return}/{@code throw} inside validator methods during adversarial review
 * (this pattern is load-bearing per Story 1.4 chunk-1 review).
 *
 * <p>Checks run in a fixed deterministic order so error messages stabilise across runs:
 *
 * <ol>
 *   <li>Required top-level keys present
 *   <li>Id regex / reserved words
 *   <li>Collection limits (fields ≤ 50, listColumns ≤ 12, statuses ≤ 10, roles ≤ 20)
 *   <li>Per-element checks (displayName length, type validity, enum literals)
 *   <li>Cross-references (listColumns → unknown field id)
 *   <li>Duplicate ids
 * </ol>
 */
@Component
public class ConfigValidator {

  /**
   * Story 4.2 AC5 — Mapping Layer validator. Constructor-injected so tests can pass {@code null}
   * via the {@link #ConfigValidator() no-arg constructor} when only stage / field / role rules are
   * exercised. The {@link #validate(RawCaseTypeConfig, YamlLineIndex, java.util.Map) overload with
   * BPMN bytes} requires this dependency to be non-null.
   */
  private final MappingValidator mappingValidator;

  public ConfigValidator() {
    this(null);
  }

  public ConfigValidator(MappingValidator mappingValidator) {
    this.mappingValidator = mappingValidator;
  }

  /**
   * Reserved stage ids — Story 3.1 AC1 (Sprint-1 triage Q4: initial set, extend on first conflict).
   * Lives here as a private constant so the validator owns the rule; if a future story needs to
   * extend, the touch-point is one place.
   */
  private static final Set<String> RESERVED_STAGE_IDS = Set.of("case", "stage", "none", "all");

  /**
   * Stage id pattern (Story 3.1 AC1) — same kebab-case shape as status / role ids, but allows a
   * 1-character id (regex {@code [a-z][a-z0-9-]{0,62}}). Stages live on the URL surface (Story 3.3)
   * so no underscore variant.
   */
  private static final java.util.regex.Pattern STAGE_ID_PATTERN =
      java.util.regex.Pattern.compile("[a-z][a-z0-9-]{0,62}");

  public ValidationResult validate(RawCaseTypeConfig raw, YamlLineIndex lines) {
    return validate(raw, lines, java.util.Map.of());
  }

  /**
   * Story 4.2 AC5 — overload that runs {@link MappingValidator} after stage validation and merges
   * its findings into the same {@link ValidationResult}. The {@code bpmnFiles} map (filename →
   * bytes) is supplied by the caller; the validator is I/O-free and never reads the filesystem.
   * Missing entries for declared {@code attachments[].file} produce {@code WKS-MAP-005}.
   */
  public ValidationResult validate(
      RawCaseTypeConfig raw, YamlLineIndex lines, java.util.Map<String, byte[]> bpmnFiles) {
    if (raw == null) {
      return ValidationResult.invalid(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_CFG_099.wire(), "YAML document is empty or could not be parsed")));
    }

    ErrorBuilder eb = new ErrorBuilder(lines);
    List<ErrorDetail> errors = new ArrayList<>();

    boolean hasId = checkRequiredString("id", raw.id(), eb, errors);
    boolean hasDisplayName = checkRequiredString("displayName", raw.displayName(), eb, errors);
    checkRequiredInt("version", raw.version(), eb, errors);
    checkWorkflow(raw.workflow(), eb, errors);

    if (hasId && !CaseTypeLimits.ID_PATTERN.matcher(raw.id()).matches()) {
      errors.add(
          eb.error(ErrorCode.WKS_CFG_009, "Id must match [a-z][a-z0-9-]{1,62} (kebab-case)", "id"));
    }
    if (hasDisplayName && raw.displayName().length() > CaseTypeLimits.MAX_DISPLAY_NAME_CHARS) {
      errors.add(
          eb.error(
              ErrorCode.WKS_CFG_007,
              "displayName must be ≤ " + CaseTypeLimits.MAX_DISPLAY_NAME_CHARS + " characters",
              "displayName"));
    }
    if (raw.description() != null && raw.description().length() > 400) {
      errors.add(
          eb.error(ErrorCode.WKS_CFG_007, "description must be ≤ 400 characters", "description"));
    }

    List<ErrorDetail> warnings = new ArrayList<>();
    List<FieldDefinition> fields = checkFields(raw.fields(), eb, errors, warnings);
    List<StatusDefinition> statuses = checkStatuses(raw.statuses(), eb, errors);
    List<RoleDefinition> roles = checkRoles(raw.roles(), eb, errors);
    List<String> listColumns =
        checkListColumns(raw.listColumns(), fields, raw.fields() != null, eb, errors);
    List<StageDefinition> stages = checkStages(raw.stages(), eb, errors);

    // Story 4.2 AC5 — Mapping Layer validation runs after stage validation, with the stage id set
    // already known. The validator is collect-all; its findings merge into {@code errors} so a
    // single ValidationResult surfaces both stage and mapping failures (no parallel call site).
    if (mappingValidator != null) {
      Set<String> stageIds = new HashSet<>();
      for (StageDefinition sd : stages) {
        stageIds.add(sd.id());
      }
      MappingValidator.Result mappingResult = mappingValidator.validate(raw, stageIds, bpmnFiles);
      errors.addAll(mappingResult.errors());
    }

    if (!errors.isEmpty()) {
      return ValidationResult.invalid(errors);
    }

    CaseTypeConfig config =
        new CaseTypeConfig(
            raw.id(),
            raw.displayName(),
            raw.version(),
            raw.description(),
            new WorkflowRef(raw.workflow().bpmn()),
            fields,
            statuses,
            listColumns,
            roles,
            stages);
    return ValidationResult.ok(config, warnings);
  }

  /**
   * Story 3.1 AC1 — validate the optional {@code stages} list. {@code null} or empty produces an
   * empty list (legal — no error). Collect-all idiom: every duplicate / pattern / reserved-word
   * violation is appended; checks never short-circuit on first error.
   */
  private List<StageDefinition> checkStages(
      List<RawCaseTypeConfig.RawStage> raws, ErrorBuilder eb, List<ErrorDetail> errors) {
    if (raws == null || raws.isEmpty()) {
      return List.of();
    }
    List<StageDefinition> out = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < raws.size(); i++) {
      RawCaseTypeConfig.RawStage s = raws.get(i);
      String base = "stages[" + i + "]";
      if (s == null || s.id() == null || s.id().isBlank()) {
        errors.add(eb.error(ErrorCode.WKS_CFG_001, "Stage entry requires 'id'", base + ".id"));
        continue;
      }
      String id = s.id();
      boolean idOk = true;
      if (!STAGE_ID_PATTERN.matcher(id).matches()) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_032, "Stage id must match [a-z][a-z0-9-]{0,62}", base + ".id"));
        idOk = false;
      }
      if (idOk && RESERVED_STAGE_IDS.contains(id)) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_033,
                "Stage id '" + id + "' is reserved (reserved: " + RESERVED_STAGE_IDS + ")",
                base + ".id"));
        idOk = false;
      }
      if (idOk && !seen.add(id)) {
        errors.add(eb.error(ErrorCode.WKS_CFG_031, "Duplicate stage id: " + id, base + ".id"));
        idOk = false;
      }
      if (idOk) {
        String displayName =
            s.displayName() == null || s.displayName().isBlank()
                ? toTitleCase(id)
                : s.displayName();
        // Story 3.1 code review S3 (2026-05-05): stage displayName shares the
        // MAX_DISPLAY_NAME_CHARS=40 cap with field/status displayName (WKS-CFG-007). Catch the
        // overflow at validate-time so it never reaches the column.
        if (displayName.length() > CaseTypeLimits.MAX_DISPLAY_NAME_CHARS) {
          errors.add(
              eb.error(
                  ErrorCode.WKS_CFG_007,
                  "displayName must be ≤ " + CaseTypeLimits.MAX_DISPLAY_NAME_CHARS + " characters",
                  base + ".displayName"));
          continue;
        }
        out.add(new StageDefinition(id, displayName, i));
      }
    }
    return out;
  }

  /**
   * Title-case a kebab-case stage id (Story 3.1 AC1): {@code "intake"} → {@code "Intake"}, {@code
   * "loan-decision"} → {@code "Loan Decision"}.
   */
  private static String toTitleCase(String kebab) {
    String[] parts = kebab.split("-");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (parts[i].isEmpty()) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(Character.toUpperCase(parts[i].charAt(0)));
      if (parts[i].length() > 1) {
        sb.append(parts[i].substring(1));
      }
    }
    return sb.toString();
  }

  // ---- per-section checks -------------------------------------------------

  private boolean checkRequiredString(
      String path, String value, ErrorBuilder eb, List<ErrorDetail> errors) {
    if (value == null || value.isBlank()) {
      errors.add(eb.error(ErrorCode.WKS_CFG_001, "Required key missing: " + path, path));
      return false;
    }
    return true;
  }

  private void checkRequiredInt(
      String path, Integer value, ErrorBuilder eb, List<ErrorDetail> errors) {
    if (value == null) {
      errors.add(eb.error(ErrorCode.WKS_CFG_001, "Required key missing: " + path, path));
    } else if (value <= 0) {
      errors.add(eb.error(ErrorCode.WKS_CFG_002, "version must be a positive integer", path));
    }
  }

  private void checkWorkflow(
      RawCaseTypeConfig.RawWorkflow wf, ErrorBuilder eb, List<ErrorDetail> errors) {
    if (wf == null) {
      errors.add(eb.error(ErrorCode.WKS_CFG_001, "Required key missing: workflow", "workflow"));
      return;
    }
    if (wf.bpmn() == null || wf.bpmn().isBlank()) {
      errors.add(
          eb.error(ErrorCode.WKS_CFG_001, "Required key missing: workflow.bpmn", "workflow.bpmn"));
    }
  }

  private List<FieldDefinition> checkFields(
      List<RawCaseTypeConfig.RawField> raws,
      ErrorBuilder eb,
      List<ErrorDetail> errors,
      List<ErrorDetail> warnings) {
    if (raws == null || raws.isEmpty()) {
      errors.add(eb.error(ErrorCode.WKS_CFG_001, "Required key missing: fields", "fields"));
      return List.of();
    }
    if (raws.size() > CaseTypeLimits.MAX_FIELDS) {
      errors.add(
          eb.error(
              ErrorCode.WKS_CFG_004,
              "fields length " + raws.size() + " exceeds maximum of " + CaseTypeLimits.MAX_FIELDS,
              "fields"));
    }

    List<FieldDefinition> out = new ArrayList<>();
    Set<String> seenIds = new HashSet<>();
    for (int i = 0; i < raws.size(); i++) {
      RawCaseTypeConfig.RawField f = raws.get(i);
      String base = "fields[" + i + "]";

      if (f == null) {
        errors.add(eb.error(ErrorCode.WKS_CFG_001, "Field entry is empty", base));
        continue;
      }

      boolean hasId = checkRequiredString(base + ".id", f.id(), eb, errors);
      boolean hasDn = checkRequiredString(base + ".displayName", f.displayName(), eb, errors);
      boolean hasType = checkRequiredString(base + ".type", f.type(), eb, errors);

      boolean idOk = hasId;
      if (hasId && !CaseTypeLimits.FIELD_ID_PATTERN.matcher(f.id()).matches()) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_009, "Field id must match [a-z][a-z0-9_-]{1,62}", base + ".id"));
        idOk = false;
      }
      if (hasId && !seenIds.add(f.id())) {
        errors.add(eb.error(ErrorCode.WKS_CFG_003, "Duplicate field id: " + f.id(), base + ".id"));
        idOk = false;
      }
      if (hasDn && f.displayName().length() > CaseTypeLimits.MAX_DISPLAY_NAME_CHARS) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_007,
                "displayName must be ≤ " + CaseTypeLimits.MAX_DISPLAY_NAME_CHARS + " characters",
                base + ".displayName"));
      }

      FieldType ft = null;
      if (hasType) {
        var parsed = FieldType.fromWire(f.type());
        if (parsed.isEmpty()) {
          errors.add(
              eb.error(
                  ErrorCode.WKS_CFG_002,
                  "Invalid field type '"
                      + f.type()
                      + "' — allowed: text|number|date|select|checkbox|textarea|file",
                  base + ".type"));
        } else {
          ft = parsed.get();
        }
      }

      List<FieldOption> opts = checkFieldOptions(f, ft, base, eb, errors);

      if (idOk && hasDn && ft != null) {
        boolean required = Boolean.TRUE.equals(f.required());
        // Default requiredOnCreate to required when YAML omits the slot — preserves seed behavior.
        boolean requiredOnCreate = f.requiredOnCreate() == null ? required : f.requiredOnCreate();
        if (requiredOnCreate && ft == FieldType.FILE) {
          warnings.add(
              eb.error(
                  ErrorCode.WKS_CFG_013,
                  "Field '"
                      + f.id()
                      + "' is type 'file' with requiredOnCreate: true, but file upload is not"
                      + " supported on the create form (Story 3.1 will close this). The create"
                      + " form will treat this field as optional until 3.1 ships.",
                  base + ".requiredOnCreate"));
        }
        out.add(
            new FieldDefinition(
                f.id(),
                f.displayName(),
                ft,
                required,
                requiredOnCreate,
                f.order() == null ? Integer.MAX_VALUE : f.order(),
                opts,
                new FieldDefinition.TypeSlots(
                    f.minLength(),
                    f.maxLength(),
                    f.min(),
                    f.max(),
                    f.step(),
                    f.dateMin(),
                    f.dateMax(),
                    f.maxBytes(),
                    f.allowedMimeTypes() == null ? List.of() : f.allowedMimeTypes())));
      }
    }

    // Preserve declared order, breaking ties by YAML order (already the list order).
    out.sort(Comparator.comparingInt(FieldDefinition::order));
    return out;
  }

  private List<FieldOption> checkFieldOptions(
      RawCaseTypeConfig.RawField f,
      FieldType ft,
      String base,
      ErrorBuilder eb,
      List<ErrorDetail> errors) {
    if (ft != FieldType.SELECT) {
      return List.of();
    }
    List<RawCaseTypeConfig.RawOption> raws = f.options();
    if (raws == null || raws.size() < CaseTypeLimits.MIN_SELECT_OPTIONS) {
      errors.add(
          eb.error(
              ErrorCode.WKS_CFG_001,
              "select fields require at least " + CaseTypeLimits.MIN_SELECT_OPTIONS + " option",
              base + ".options"));
      return List.of();
    }
    if (raws.size() > CaseTypeLimits.MAX_SELECT_OPTIONS) {
      errors.add(
          eb.error(
              ErrorCode.WKS_CFG_004,
              "options length "
                  + raws.size()
                  + " exceeds maximum of "
                  + CaseTypeLimits.MAX_SELECT_OPTIONS,
              base + ".options"));
    }
    List<FieldOption> out = new ArrayList<>();
    Set<String> seenValues = new HashSet<>();
    for (int i = 0; i < raws.size(); i++) {
      RawCaseTypeConfig.RawOption o = raws.get(i);
      String opath = base + ".options[" + i + "]";
      if (o == null) {
        errors.add(
            eb.error(ErrorCode.WKS_CFG_001, "option requires both 'label' and 'value'", opath));
        continue;
      }
      boolean labelOk = o.label() != null && !o.label().isBlank();
      boolean valueOk = o.value() != null && !o.value().isBlank();
      if (!labelOk) {
        errors.add(
            eb.error(ErrorCode.WKS_CFG_001, "option label must not be blank", opath + ".label"));
      }
      if (!valueOk) {
        errors.add(
            eb.error(ErrorCode.WKS_CFG_001, "option value must not be blank", opath + ".value"));
      }
      if (!labelOk || !valueOk) {
        continue;
      }
      if (!seenValues.add(o.value())) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_003, "Duplicate option value: " + o.value(), opath + ".value"));
        continue;
      }
      out.add(new FieldOption(o.label(), o.value()));
    }
    return out;
  }

  private List<StatusDefinition> checkStatuses(
      List<RawCaseTypeConfig.RawStatus> raws, ErrorBuilder eb, List<ErrorDetail> errors) {
    if (raws == null || raws.isEmpty()) {
      errors.add(eb.error(ErrorCode.WKS_CFG_001, "Required key missing: statuses", "statuses"));
      return List.of();
    }
    if (raws.size() > CaseTypeLimits.MAX_STATUSES) {
      errors.add(
          eb.error(
              ErrorCode.WKS_CFG_006,
              "statuses length "
                  + raws.size()
                  + " exceeds maximum of "
                  + CaseTypeLimits.MAX_STATUSES,
              "statuses"));
    }
    List<StatusDefinition> out = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < raws.size(); i++) {
      RawCaseTypeConfig.RawStatus s = raws.get(i);
      String base = "statuses[" + i + "]";
      if (s == null) {
        errors.add(eb.error(ErrorCode.WKS_CFG_001, "Status entry is empty", base));
        continue;
      }
      boolean hasId = checkRequiredString(base + ".id", s.id(), eb, errors);
      boolean hasDn = checkRequiredString(base + ".displayName", s.displayName(), eb, errors);
      if (hasId && !CaseTypeLimits.ID_PATTERN.matcher(s.id()).matches()) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_009, "Status id must match [a-z][a-z0-9-]{1,62}", base + ".id"));
      }
      if (hasId && !seen.add(s.id())) {
        errors.add(eb.error(ErrorCode.WKS_CFG_003, "Duplicate status id: " + s.id(), base + ".id"));
      }
      if (hasDn && s.displayName().length() > CaseTypeLimits.MAX_DISPLAY_NAME_CHARS) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_007,
                "displayName must be ≤ " + CaseTypeLimits.MAX_DISPLAY_NAME_CHARS + " characters",
                base + ".displayName"));
      }
      StatusColor color = null;
      if (s.color() != null) {
        var parsed = StatusColor.fromWire(s.color());
        if (parsed.isEmpty()) {
          errors.add(
              eb.error(
                  ErrorCode.WKS_CFG_008,
                  "Unknown status color '"
                      + s.color()
                      + "' — allowed: blue|amber|violet|emerald|zinc|red|cyan|rose|indigo|teal",
                  base + ".color"));
        } else {
          color = parsed.get();
        }
      }
      if (hasId && hasDn) {
        out.add(new StatusDefinition(s.id(), s.displayName(), color));
      }
    }
    return out;
  }

  private List<RoleDefinition> checkRoles(
      List<RawCaseTypeConfig.RawRole> raws, ErrorBuilder eb, List<ErrorDetail> errors) {
    if (raws == null || raws.isEmpty()) {
      errors.add(eb.error(ErrorCode.WKS_CFG_001, "Required key missing: roles", "roles"));
      return List.of();
    }
    if (raws.size() > CaseTypeLimits.MAX_ROLES) {
      errors.add(
          eb.error(
              ErrorCode.WKS_CFG_004,
              "roles length " + raws.size() + " exceeds maximum of " + CaseTypeLimits.MAX_ROLES,
              "roles"));
    }
    List<RoleDefinition> out = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < raws.size(); i++) {
      RawCaseTypeConfig.RawRole r = raws.get(i);
      String base = "roles[" + i + "]";
      if (r == null) {
        errors.add(eb.error(ErrorCode.WKS_CFG_001, "Role entry is empty", base));
        continue;
      }
      boolean hasName = checkRequiredString(base + ".name", r.name(), eb, errors);
      boolean nameOk = hasName;
      if (hasName && !CaseTypeLimits.ID_PATTERN.matcher(r.name()).matches()) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_009,
                "Role name must match [a-z][a-z0-9-]{1,62}",
                base + ".name"));
        nameOk = false;
      }
      if (hasName && !seen.add(r.name())) {
        errors.add(
            eb.error(ErrorCode.WKS_CFG_003, "Duplicate role name: " + r.name(), base + ".name"));
        nameOk = false;
      }

      List<String> rawPerms = r.permissions();
      List<Permission> perms = new ArrayList<>();
      if (rawPerms == null || rawPerms.isEmpty()) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_001,
                "Role requires at least one permission",
                base + ".permissions"));
      } else {
        if (rawPerms.size() > CaseTypeLimits.MAX_PERMISSIONS_PER_ROLE) {
          errors.add(
              eb.error(
                  ErrorCode.WKS_CFG_004,
                  "permissions length "
                      + rawPerms.size()
                      + " exceeds maximum of "
                      + CaseTypeLimits.MAX_PERMISSIONS_PER_ROLE,
                  base + ".permissions"));
        }
        for (int j = 0; j < rawPerms.size(); j++) {
          String v = rawPerms.get(j);
          var parsed = Permission.fromWire(v);
          if (parsed.isEmpty()) {
            String allow =
                java.util.Arrays.stream(Permission.values())
                    .map(Permission::wire)
                    .collect(java.util.stream.Collectors.joining("|"));
            errors.add(
                eb.error(
                    ErrorCode.WKS_CFG_008,
                    "Unknown permission '" + v + "' — allowed: " + allow,
                    base + ".permissions[" + j + "]"));
          } else {
            perms.add(parsed.get());
          }
        }
      }
      boolean permsOk = rawPerms != null && !rawPerms.isEmpty() && perms.size() == rawPerms.size();
      if (nameOk && permsOk) {
        out.add(new RoleDefinition(r.name(), perms));
      }
    }
    return out;
  }

  private List<String> checkListColumns(
      List<String> raws,
      List<FieldDefinition> fields,
      boolean fieldsParentPresent,
      ErrorBuilder eb,
      List<ErrorDetail> errors) {
    if (raws == null || raws.isEmpty()) {
      errors.add(
          eb.error(ErrorCode.WKS_CFG_001, "Required key missing: listColumns", "listColumns"));
      return List.of();
    }
    if (raws.size() > CaseTypeLimits.MAX_LIST_COLUMNS) {
      errors.add(
          eb.error(
              ErrorCode.WKS_CFG_005,
              "listColumns length "
                  + raws.size()
                  + " exceeds maximum of "
                  + CaseTypeLimits.MAX_LIST_COLUMNS,
              "listColumns"));
    }
    Set<String> fieldIds = new LinkedHashSet<>();
    for (FieldDefinition f : fields) {
      fieldIds.add(f.id());
    }
    Set<String> seen = new HashSet<>();
    List<String> out = new ArrayList<>();
    for (int i = 0; i < raws.size(); i++) {
      String ref = raws.get(i);
      String path = "listColumns[" + i + "]";
      if (ref == null || ref.isBlank()) {
        errors.add(eb.error(ErrorCode.WKS_CFG_001, "listColumn entry is empty", path));
        continue;
      }
      boolean refOk = true;
      if (!seen.add(ref)) {
        errors.add(eb.error(ErrorCode.WKS_CFG_003, "Duplicate listColumn: " + ref, path));
        refOk = false;
      }
      if (fieldsParentPresent
          && !fieldIds.contains(ref)
          && !CaseTypeLimits.SYSTEM_LIST_COLUMNS.contains(ref)) {
        errors.add(
            eb.error(
                ErrorCode.WKS_CFG_005,
                "listColumn references unknown field id or system column: " + ref,
                path));
        refOk = false;
      }
      if (refOk) {
        out.add(ref);
      }
    }
    return out;
  }
}
