package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.CaseTypeVersionRecord;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.FieldAction;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.FieldMapping;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.IrreconcilableItem;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.IrreconcilableKind;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.StatusAction;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.StatusMapping;
import com.wkspower.platform.domain.event.RebaseApplied;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConcurrentModificationException;
import com.wkspower.platform.domain.exception.WksConfigException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Story 3.9 — domain service that orchestrates CaseType version rebase for a single in-flight Case.
 *
 * <p>Exposes two public methods:
 *
 * <ul>
 *   <li>{@link #dryRun(String, UUID, int)} — compute the rebase report without mutating DB state.
 *   <li>{@link #apply(String, UUID, int)} — compute the report, assert no irreconcilable items,
 *       then mutate {@code cases.case_type_version} via {@link CaseRepository#save}. The caller
 *       (AdminController) is responsible for emitting the audit log AFTER this method returns and
 *       for wrapping the call in a {@code @Transactional} boundary (see memory {@code
 *       feedback_transactional_db_exception_postgres.md} — catching a DB exception inside a
 *       transaction and then making another DB call aborts on Postgres).
 * </ul>
 *
 * <p>This class is framework-free (no Spring imports). Wired into the Spring context via {@link
 * com.wkspower.platform.infrastructure.config.ConfigServiceConfig}.
 */
public class CaseRebaseService {

  private final CaseRepository caseRepository;
  private final CaseTypeVersionRegistry versionRegistry;
  private final CaseTypeSource caseTypeSource;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public CaseRebaseService(
      CaseRepository caseRepository,
      CaseTypeVersionRegistry versionRegistry,
      CaseTypeSource caseTypeSource,
      EventPublisher eventPublisher,
      Clock clock) {
    this.caseRepository = caseRepository;
    this.versionRegistry = versionRegistry;
    this.caseTypeSource = caseTypeSource;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  /**
   * Dry-run rebase: compute the structured mapping report without mutating any DB state.
   *
   * @param caseTypeId the CaseType id from the path parameter
   * @param caseId the Case id (UUID)
   * @param toVersion the target CaseType version number
   * @return structured {@link CaseRebaseReport} with {@code applied=false}
   * @throws WksNotFoundException when the case id does not resolve to any row
   * @throws WksConfigException with {@link ErrorCode#WKS_API_007} when the caseTypeId path
   *     parameter does not match the case's bound caseTypeId, or when toVersion is invalid
   */
  public CaseRebaseReport dryRun(String caseTypeId, UUID caseId, int toVersion) {
    Case kase = resolveCase(caseTypeId, caseId);
    int fromVersion = kase.caseTypeVersion();
    validateVersionArg(caseTypeId, fromVersion, toVersion);

    CaseTypeConfig fromConfig = loadConfig(caseTypeId, fromVersion);
    CaseTypeConfig toConfig = loadConfig(caseTypeId, toVersion);

    return buildReport(kase, fromVersion, toVersion, fromConfig, toConfig, false);
  }

  /**
   * Apply rebase: compute the report, assert {@code irreconcilable} is empty, then atomically
   * update {@code cases.case_type_version}.
   *
   * <p><b>Transactional contract:</b> the caller (AdminController) MUST wrap this method in a
   * {@code @Transactional} boundary. This method does NOT declare {@code @Transactional} itself
   * because it lives in the domain layer (framework-free). The audit log MUST be emitted by the
   * controller AFTER this method returns (post-transaction, so no audit entry is emitted on
   * failure).
   *
   * @param caseTypeId the CaseType id from the path parameter
   * @param caseId the Case id (UUID)
   * @param toVersion the target CaseType version number
   * @return structured {@link CaseRebaseReport} with {@code applied=true}
   * @throws WksNotFoundException when the case id does not resolve to any row
   * @throws WksConfigException with {@link ErrorCode#WKS_API_007} on reverse toVersion
   * @throws WksConfigException with {@link ErrorCode#WKS_API_008} on no-op toVersion
   * @throws WksConfigException with {@link ErrorCode#WKS_CFG_034} when irreconcilable items exist
   * @throws WksConcurrentModificationException with {@link ErrorCode#WKS_CFG_035} when the
   *     version-checked update finds the row has been bumped concurrently
   */
  public CaseRebaseReport apply(String caseTypeId, UUID caseId, int toVersion) {
    return apply(caseTypeId, caseId, toVersion, "ANONYMOUS", null);
  }

  /**
   * Overload that accepts {@code actor} + {@code requestId} for the {@link RebaseApplied} event so
   * the AFTER_COMMIT audit listener has the operator identity available. The no-arg overload is
   * retained for tests that don't need to assert on the event payload.
   */
  public CaseRebaseReport apply(
      String caseTypeId, UUID caseId, int toVersion, String actor, String requestId) {
    Case kase = resolveCase(caseTypeId, caseId);
    int fromVersion = kase.caseTypeVersion();
    validateVersionArg(caseTypeId, fromVersion, toVersion);

    CaseTypeConfig fromConfig = loadConfig(caseTypeId, fromVersion);
    CaseTypeConfig toConfig = loadConfig(caseTypeId, toVersion);

    CaseRebaseReport report =
        buildReport(kase, fromVersion, toVersion, fromConfig, toConfig, false);

    if (!report.irreconcilable().isEmpty()) {
      throw new WksConfigException(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_CFG_034.wire(),
                  "Rebase aborted — "
                      + report.irreconcilable().size()
                      + " irreconcilable item(s) require manual decision. Inspect dry-run"
                      + " report and resolve before retrying.")),
          Map.of("irreconcilable", report.irreconcilable()));
    }

    // Story 3.9 review remediation — version-checked update. Closes the TOCTOU window between
    // resolveCase() and the write: a concurrent UPDATE that bumps c.version causes this UPDATE to
    // match zero rows, surfacing as WKS-CFG-035 instead of silently winning via JPA merge.
    int affected = caseRepository.updateCaseTypeVersion(kase.id(), toVersion, kase.version());
    if (affected == 0) {
      throw new WksConcurrentModificationException(
          ErrorCode.WKS_CFG_035,
          "case modified concurrently during rebase (caseId="
              + kase.id()
              + ", expectedVersion="
              + kase.version()
              + ") — reload and retry");
    }

    // Publish the domain event INSIDE the surrounding @Transactional. The AFTER_COMMIT listener
    // (RebaseAuditListener) emits the audit log line only after commit — see memory
    // feedback_transactional_db_exception_postgres.md. Rollback ⇒ no audit line.
    if (eventPublisher != null) {
      eventPublisher.publish(
          new RebaseApplied(
              kase.id(),
              kase.caseTypeId(),
              fromVersion,
              toVersion,
              "migration-rebase",
              "migration-rebase",
              actor,
              requestId,
              clock.now()));
    }

    return buildReport(kase, fromVersion, toVersion, fromConfig, toConfig, true);
  }

  // -------------------------------------------------------------------------
  // Internal helpers
  // -------------------------------------------------------------------------

  private Case resolveCase(String caseTypeId, UUID caseId) {
    Optional<Case> found = caseRepository.findById(caseId);
    if (found.isEmpty()) {
      throw new WksNotFoundException("Case not found: " + caseId);
    }
    Case kase = found.get();
    if (!caseTypeId.equals(kase.caseTypeId())) {
      throw new WksConfigException(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_API_007.wire(),
                  "caseTypeId path parameter '"
                      + caseTypeId
                      + "' does not match the case's bound caseTypeId '"
                      + kase.caseTypeId()
                      + "'")));
    }
    return kase;
  }

  private void validateVersionArg(String caseTypeId, int fromVersion, int toVersion) {
    // Story 3.9 review remediation — split no-op (toVersion == fromVersion) from reverse
    // (toVersion < fromVersion) so SI devs grep distinct wire codes for distinct surfaces.
    if (toVersion == fromVersion) {
      throw new WksConfigException(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_API_008.wire(),
                  "toVersion equals current caseTypeVersion — no-op rebase rejected"
                      + " (current="
                      + fromVersion
                      + ", requested="
                      + toVersion
                      + ")")));
    }
    if (toVersion < fromVersion) {
      throw new WksConfigException(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_API_007.wire(),
                  "toVersion must be strictly greater than current caseTypeVersion"
                      + " (current="
                      + fromVersion
                      + ", requested="
                      + toVersion
                      + ")")));
    }
    Optional<CaseTypeVersionRecord> record = versionRegistry.findVersion(caseTypeId, toVersion);
    if (record.isEmpty()) {
      throw new WksConfigException(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_API_007.wire(),
                  "toVersion "
                      + toVersion
                      + " not found in case_type_versions for caseTypeId "
                      + caseTypeId)));
    }
  }

  private CaseTypeConfig loadConfig(String caseTypeId, int version) {
    Optional<CaseTypeVersionRecord> record = versionRegistry.findVersion(caseTypeId, version);
    if (record.isEmpty() || record.get().rawYaml() == null) {
      throw new WksConfigException(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_CFG_099.wire(),
                  "CaseType version record missing or has null YAML bytes for caseTypeId="
                      + caseTypeId
                      + " version="
                      + version)));
    }
    byte[] yaml = record.get().rawYaml();
    String sourceName = "rebase:" + caseTypeId + ":" + version;

    // Use lenient-aware load (Story 3.11 pattern) so schema-drifted older versions still load.
    ValidationResult strict = caseTypeSource.loadBytes(sourceName, yaml);
    if (strict.config().isPresent()) {
      return strict.config().get();
    }
    ValidationResult lenient = caseTypeSource.loadBytesLenient(sourceName, yaml);
    if (lenient.config().isPresent()) {
      return lenient.config().get();
    }
    throw new WksConfigException(
        List.of(
            ErrorDetail.of(
                ErrorCode.WKS_CFG_099.wire(),
                "CaseType version v"
                    + version
                    + " for caseTypeId="
                    + caseTypeId
                    + " could not be re-parsed (strict + lenient both failed)")));
  }

  /**
   * Build the {@link CaseRebaseReport} from the two configs and the case snapshot. Does NOT mutate
   * any state. The {@code applied} flag is set by the caller to distinguish dry-run from apply.
   */
  private CaseRebaseReport buildReport(
      Case kase,
      int fromVersion,
      int toVersion,
      CaseTypeConfig fromConfig,
      CaseTypeConfig toConfig,
      boolean applied) {

    Map<String, FieldDefinition> fromFields = indexFields(fromConfig);
    Map<String, FieldDefinition> toFields = indexFields(toConfig);
    Map<String, Object> caseData = kase.data();

    List<FieldMapping> fieldMappings = new ArrayList<>();
    List<IrreconcilableItem> irreconcilable = new ArrayList<>();

    // Fields in the FROM version
    for (Map.Entry<String, FieldDefinition> entry : fromFields.entrySet()) {
      String fieldId = entry.getKey();
      FieldDefinition fromField = entry.getValue();
      FieldDefinition toField = toFields.get(fieldId);

      if (toField == null) {
        // Field removed in target version
        Object currentValue = caseData.get(fieldId);
        if (currentValue != null) {
          // Non-null data on a removed field → irreconcilable
          irreconcilable.add(
              new IrreconcilableItem(
                  IrreconcilableKind.REMOVED_FIELD_WITH_DATA, fieldId, null, "<redacted>"));
        }
        fieldMappings.add(
            new FieldMapping(fieldId, FieldAction.DROP, typeName(fromField), null, null));
      } else if (fromField.type() != toField.type()) {
        // Field type changed → MANUAL (does not block apply in Phase-0)
        fieldMappings.add(
            new FieldMapping(
                fieldId, FieldAction.MANUAL, typeName(fromField), typeName(toField), null));
      } else {
        // Field retained with same type
        fieldMappings.add(
            new FieldMapping(
                fieldId, FieldAction.KEEP, typeName(fromField), typeName(toField), null));
      }
    }

    // Fields added in the TO version (not in FROM)
    for (Map.Entry<String, FieldDefinition> entry : toFields.entrySet()) {
      String fieldId = entry.getKey();
      if (!fromFields.containsKey(fieldId)) {
        FieldDefinition toField = entry.getValue();
        fieldMappings.add(
            new FieldMapping(fieldId, FieldAction.DEFAULT, null, typeName(toField), null));
      }
    }

    // Status mappings
    Set<String> fromStatuses = collectAllStatusIds(fromConfig);
    Set<String> toStatuses = collectAllStatusIds(toConfig);
    String currentStatus = kase.status();

    List<StatusMapping> statusMappings = new ArrayList<>();
    for (String statusId : fromStatuses) {
      if (toStatuses.contains(statusId)) {
        statusMappings.add(new StatusMapping(statusId, StatusAction.KEEP, null));
      } else {
        // Status removed from target version
        statusMappings.add(new StatusMapping(statusId, StatusAction.MANUAL, null));
        // If this is the case's current status → irreconcilable
        if (statusId.equals(currentStatus)) {
          irreconcilable.add(
              new IrreconcilableItem(
                  IrreconcilableKind.STATUS_HAS_NO_EQUIVALENT, null, statusId, null));
        }
      }
    }

    // Story 3.9 review remediation — stage-id rename detection. When the case currently sits on a
    // stage S in fromVersion and toVersion does NOT declare a stage with the same id, the operator
    // must manually decide where the case lands. Phase-0 surfaces this as irreconcilable; Story
    // 3-9.1 will add operator-supplied mapping JSON for stage remap.
    String currentStageId = kase.currentStageId();
    if (currentStageId != null && !currentStageId.isBlank()) {
      Set<String> toStageIds = collectStageIds(toConfig);
      Set<String> fromStageIds = collectStageIds(fromConfig);
      if (fromStageIds.contains(currentStageId) && !toStageIds.contains(currentStageId)) {
        irreconcilable.add(
            new IrreconcilableItem(
                IrreconcilableKind.STAGE_REMOVED_WITH_ACTIVE_CASE, null, null, currentStageId));
      }
    }

    return new CaseRebaseReport(
        kase.id(),
        kase.caseTypeId(),
        fromVersion,
        toVersion,
        applied,
        fieldMappings,
        statusMappings,
        irreconcilable);
  }

  private static Set<String> collectStageIds(CaseTypeConfig config) {
    Set<String> ids = new HashSet<>();
    if (config.stages() != null) {
      for (StageDefinition stage : config.stages()) {
        ids.add(stage.id());
      }
    }
    return ids;
  }

  private static Map<String, FieldDefinition> indexFields(CaseTypeConfig config) {
    Map<String, FieldDefinition> map = new HashMap<>();
    if (config.fields() != null) {
      for (FieldDefinition f : config.fields()) {
        map.put(f.id(), f);
      }
    }
    return map;
  }

  private static Set<String> collectAllStatusIds(CaseTypeConfig config) {
    Set<String> ids = new HashSet<>();
    if (config.statuses() != null) {
      for (StatusDefinition s : config.statuses()) {
        ids.add(s.id());
      }
    }
    if (config.stages() != null) {
      for (StageDefinition stage : config.stages()) {
        if (stage.statuses() != null) {
          for (StatusDefinition s : stage.statuses()) {
            ids.add(s.id());
          }
        }
      }
    }
    return ids;
  }

  private static String typeName(FieldDefinition field) {
    return field.type() != null ? field.type().name() : null;
  }
}
