package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.FieldAction;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.IrreconcilableKind;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.StatusAction;
import com.wkspower.platform.domain.event.RebaseApplied;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksConcurrentModificationException;
import com.wkspower.platform.domain.exception.WksConfigException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Story 3.9 — unit tests for {@link CaseRebaseService}. Pure-Java, no Spring context. Exercises all
 * 6 cases from Task 5.1: 1. dry-run additive (clean) 2. dry-run with REMOVED_FIELD_WITH_DATA 3.
 * dry-run with STATUS_HAS_NO_EQUIVALENT 4. apply success 5. apply blocked by irreconcilable 6.
 * apply blocked by reverse-version
 */
class CaseRebaseServiceTest {

  private static final String CT_ID = "test-ct";
  private static final UUID CASE_ID = UUID.randomUUID();
  private static final UUID USER_ID = UUID.randomUUID();

  private FakeCaseTypeVersionRegistry versionRegistry;
  private FakeCaseRepository caseRepository;
  private FakeCaseTypeSource caseTypeSource;
  private RecordingEventPublisher eventPublisher;
  private CaseRebaseService service;

  @BeforeEach
  void setUp() {
    versionRegistry = new FakeCaseTypeVersionRegistry();
    caseRepository = new FakeCaseRepository();
    caseTypeSource = new FakeCaseTypeSource();
    eventPublisher = new RecordingEventPublisher();
    Clock clock = () -> Instant.parse("2026-05-11T00:00:00Z");
    service =
        new CaseRebaseService(
            caseRepository, versionRegistry, caseTypeSource, eventPublisher, clock);
  }

  // ---- Helper: minimal CaseTypeConfig ----

  private static byte[] v1Yaml() {
    return """
        id: test-ct
        version: 1
        displayName: Test CT
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: closed
            displayName: Closed
            color: green
            terminal: true
        fields:
          - id: name
            displayName: Name
            type: text
            required: true
        roles: []
        """
        .getBytes(StandardCharsets.UTF_8);
  }

  private static byte[] v2Yaml() {
    return """
        id: test-ct
        version: 2
        displayName: Test CT
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: closed
            displayName: Closed
            color: green
            terminal: true
        fields:
          - id: name
            displayName: Name
            type: text
            required: true
          - id: email
            displayName: Email
            type: text
            required: false
        roles: []
        """
        .getBytes(StandardCharsets.UTF_8);
  }

  private static CaseTypeConfig minimalConfig(
      String id, int version, List<StatusDefinition> statuses, List<FieldDefinition> fields) {
    return new CaseTypeConfig(
        id, "Test CT", version, null, null, fields, statuses, List.of(), List.of(), List.of(),
        List.of());
  }

  private static CaseTypeConfig configWithStages(
      String id,
      int version,
      List<StatusDefinition> statuses,
      List<FieldDefinition> fields,
      List<StageDefinition> stages) {
    return new CaseTypeConfig(
        id, "Test CT", version, null, null, fields, statuses, List.of(), List.of(), stages,
        List.of());
  }

  private Case makeCase(int caseTypeVersion, Map<String, Object> data) {
    return new Case(
        CASE_ID,
        CT_ID,
        caseTypeVersion,
        "open",
        null,
        data,
        null,
        Instant.now(),
        USER_ID,
        Instant.now(),
        0L,
        null,
        null);
  }

  // ---- Test 1: dry-run additive (clean) ----

  @Test
  void dryRun_additiveChange_cleanReport() {
    // v1 has name field; v2 adds email field
    StatusDefinition open = new StatusDefinition("open", "Open", StatusColor.BLUE);
    StatusDefinition closed = new StatusDefinition("closed", "Closed", StatusColor.EMERALD);
    FieldDefinition nameField =
        new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, null, null);
    FieldDefinition emailField =
        new FieldDefinition("email", "Email", FieldType.TEXT, false, 1, null, null);

    CaseTypeConfig fromConfig = minimalConfig(CT_ID, 1, List.of(open, closed), List.of(nameField));
    CaseTypeConfig toConfig =
        minimalConfig(CT_ID, 2, List.of(open, closed), List.of(nameField, emailField));

    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":1", fromConfig);
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":2", toConfig);

    caseRepository.save(makeCase(1, Map.of("name", "Alice")));

    CaseRebaseReport report = service.dryRun(CT_ID, CASE_ID, 2);

    assertThat(report.fromVersion()).isEqualTo(1);
    assertThat(report.toVersion()).isEqualTo(2);
    assertThat(report.applied()).isFalse();
    assertThat(report.irreconcilable()).isEmpty();

    // name field should be KEEP; email should be DEFAULT
    assertThat(report.fieldMappings()).hasSize(2);
    var nameMapping =
        report.fieldMappings().stream()
            .filter(f -> "name".equals(f.fieldId()))
            .findFirst()
            .orElseThrow();
    assertThat(nameMapping.action()).isEqualTo(FieldAction.KEEP);
    var emailMapping =
        report.fieldMappings().stream()
            .filter(f -> "email".equals(f.fieldId()))
            .findFirst()
            .orElseThrow();
    assertThat(emailMapping.action()).isEqualTo(FieldAction.DEFAULT);

    // both statuses KEEP
    assertThat(report.statusMappings()).hasSize(2);
    assertThat(report.statusMappings()).allMatch(s -> s.action() == StatusAction.KEEP);

    // DB not mutated (case still at v1)
    assertThat(caseRepository.findById(CASE_ID).orElseThrow().caseTypeVersion()).isEqualTo(1);
  }

  // ---- Test 2: dry-run with REMOVED_FIELD_WITH_DATA ----

  @Test
  void dryRun_removedFieldWithData_irreconcilable() {
    StatusDefinition open = new StatusDefinition("open", "Open", StatusColor.BLUE);
    FieldDefinition nameField =
        new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, null, null);
    FieldDefinition panField =
        new FieldDefinition("panNumber", "PAN", FieldType.TEXT, false, 1, null, null);

    CaseTypeConfig fromConfig =
        minimalConfig(CT_ID, 1, List.of(open), List.of(nameField, panField));
    CaseTypeConfig toConfig =
        minimalConfig(CT_ID, 2, List.of(open), List.of(nameField)); // panNumber removed

    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":1", fromConfig);
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":2", toConfig);

    // Case has panNumber with data
    caseRepository.save(makeCase(1, Map.of("name", "Alice", "panNumber", "ABCDE1234F")));

    CaseRebaseReport report = service.dryRun(CT_ID, CASE_ID, 2);

    assertThat(report.applied()).isFalse();
    assertThat(report.irreconcilable()).hasSize(1);
    var item = report.irreconcilable().get(0);
    assertThat(item.kind()).isEqualTo(IrreconcilableKind.REMOVED_FIELD_WITH_DATA);
    assertThat(item.fieldId()).isEqualTo("panNumber");
    assertThat(item.currentValue()).isEqualTo("<redacted>");
  }

  // ---- Test 3: dry-run with STATUS_HAS_NO_EQUIVALENT ----

  @Test
  void dryRun_currentStatusRemovedInTarget_irreconcilable() {
    StatusDefinition open = new StatusDefinition("open", "Open", StatusColor.BLUE);
    StatusDefinition under = new StatusDefinition("under-review", "Under Review", StatusColor.BLUE);
    FieldDefinition nameField =
        new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, null, null);

    CaseTypeConfig fromConfig = minimalConfig(CT_ID, 1, List.of(open, under), List.of(nameField));
    CaseTypeConfig toConfig =
        minimalConfig(CT_ID, 2, List.of(open), List.of(nameField)); // under-review removed

    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":1", fromConfig);
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":2", toConfig);

    // Case is currently in under-review status (which is removed in v2)
    Case kase =
        new Case(
            CASE_ID,
            CT_ID,
            1,
            "under-review",
            null,
            Map.of("name", "Bob"),
            null,
            Instant.now(),
            USER_ID,
            Instant.now(),
            0L,
            null,
            null);
    caseRepository.save(kase);

    CaseRebaseReport report = service.dryRun(CT_ID, CASE_ID, 2);

    assertThat(report.irreconcilable()).hasSize(1);
    var item = report.irreconcilable().get(0);
    assertThat(item.kind()).isEqualTo(IrreconcilableKind.STATUS_HAS_NO_EQUIVALENT);
    assertThat(item.statusId()).isEqualTo("under-review");
  }

  // ---- Test 4: apply success ----

  @Test
  void apply_noIrreconcilable_mutatesCaseTypeVersion() {
    StatusDefinition open = new StatusDefinition("open", "Open", StatusColor.BLUE);
    FieldDefinition nameField =
        new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, null, null);
    FieldDefinition emailField =
        new FieldDefinition("email", "Email", FieldType.TEXT, false, 1, null, null);

    CaseTypeConfig fromConfig = minimalConfig(CT_ID, 1, List.of(open), List.of(nameField));
    CaseTypeConfig toConfig =
        minimalConfig(CT_ID, 2, List.of(open), List.of(nameField, emailField));

    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":1", fromConfig);
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":2", toConfig);

    caseRepository.save(makeCase(1, Map.of("name", "Alice")));

    CaseRebaseReport report = service.apply(CT_ID, CASE_ID, 2);

    assertThat(report.applied()).isTrue();
    assertThat(report.fromVersion()).isEqualTo(1);
    assertThat(report.toVersion()).isEqualTo(2);
    assertThat(report.irreconcilable()).isEmpty();

    // DB mutated — case now at v2
    assertThat(caseRepository.findById(CASE_ID).orElseThrow().caseTypeVersion()).isEqualTo(2);
  }

  // ---- Test 5: apply blocked by irreconcilable ----

  @Test
  void apply_withIrreconcilable_throwsCfg034() {
    StatusDefinition open = new StatusDefinition("open", "Open", StatusColor.BLUE);
    FieldDefinition nameField =
        new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, null, null);
    FieldDefinition legacyField =
        new FieldDefinition("legacyField", "Legacy", FieldType.TEXT, false, 1, null, null);

    CaseTypeConfig fromConfig =
        minimalConfig(CT_ID, 1, List.of(open), List.of(nameField, legacyField));
    CaseTypeConfig toConfig = minimalConfig(CT_ID, 2, List.of(open), List.of(nameField));

    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":1", fromConfig);
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":2", toConfig);

    caseRepository.save(makeCase(1, Map.of("name", "Alice", "legacyField", "data")));

    assertThatThrownBy(() -> service.apply(CT_ID, CASE_ID, 2))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors()).hasSize(1);
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_CFG_034.wire());
              assertThat(wce.getErrors().get(0).message()).contains("irreconcilable");
            });

    // Case not mutated — still at v1
    assertThat(caseRepository.findById(CASE_ID).orElseThrow().caseTypeVersion()).isEqualTo(1);
  }

  // ---- Test 6: apply blocked by reverse-version ----

  @Test
  void dryRun_reverseVersion_throwsApi007() {
    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());

    caseRepository.save(makeCase(2, Map.of()));

    assertThatThrownBy(() -> service.dryRun(CT_ID, CASE_ID, 1))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_API_007.wire());
              assertThat(wce.getErrors().get(0).message()).contains("strictly greater");
            });
  }

  // ---- Test: case not found ----

  @Test
  void dryRun_caseNotFound_throwsNotFound() {
    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());

    assertThatThrownBy(() -> service.dryRun(CT_ID, UUID.randomUUID(), 2))
        .isInstanceOf(WksNotFoundException.class);
  }

  // ---- Test: toVersion not in registry ----

  @Test
  void dryRun_nonExistentToVersion_throwsApi007() {
    versionRegistry.seed(CT_ID, 1, v1Yaml());

    caseRepository.save(makeCase(1, Map.of()));

    assertThatThrownBy(() -> service.dryRun(CT_ID, CASE_ID, 99))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_API_007.wire());
              assertThat(wce.getErrors().get(0).message())
                  .contains("not found in case_type_versions");
            });
  }

  // ---- Test: caseTypeId mismatch ----

  @Test
  void dryRun_caseTypeIdMismatch_throwsApi007() {
    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());

    // Case is bound to "other-ct", not "test-ct"
    Case wrongCt =
        new Case(
            CASE_ID,
            "other-ct",
            1,
            "open",
            null,
            Map.of(),
            null,
            Instant.now(),
            USER_ID,
            Instant.now(),
            0L,
            null,
            null);
    caseRepository.save(wrongCt);

    assertThatThrownBy(() -> service.dryRun(CT_ID, CASE_ID, 2))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_API_007.wire());
              assertThat(wce.getErrors().get(0).message()).contains("does not match");
            });
  }

  // ---- Test: concurrent modification detected by version-checked update ----

  @Test
  void apply_concurrentVersionBump_throwsCfg035() {
    StatusDefinition open = new StatusDefinition("open", "Open", StatusColor.BLUE);
    FieldDefinition nameField =
        new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, null, null);

    CaseTypeConfig fromConfig = minimalConfig(CT_ID, 1, List.of(open), List.of(nameField));
    CaseTypeConfig toConfig = minimalConfig(CT_ID, 2, List.of(open), List.of(nameField));

    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":1", fromConfig);
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":2", toConfig);

    caseRepository.save(makeCase(1, Map.of("name", "Alice")));
    // Simulate a concurrent UPDATE bumping the @Version column AFTER the service's findById
    // (resolveCase) but BEFORE updateCaseTypeVersion fires. The hook is triggered inside the
    // fake's updateCaseTypeVersion as the first thing, preserving TOCTOU ordering.
    caseRepository.simulateConcurrentBumpOnNextUpdate = true;

    assertThatThrownBy(() -> service.apply(CT_ID, CASE_ID, 2))
        .isInstanceOf(WksConcurrentModificationException.class)
        .satisfies(
            ex -> {
              WksConcurrentModificationException wce = (WksConcurrentModificationException) ex;
              assertThat(wce.getCode()).isEqualTo(ErrorCode.WKS_CFG_035.wire());
              assertThat(wce.getMessage()).contains("modified concurrently");
            });

    // No event emitted on the failure path
    assertThat(eventPublisher.events).isEmpty();
  }

  // ---- Test: stage-removed-with-active-case detected as irreconcilable ----

  @Test
  void dryRun_stageRemovedWithActiveCase_irreconcilable() {
    StatusDefinition open = new StatusDefinition("open", "Open", StatusColor.BLUE);
    FieldDefinition nameField =
        new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, null, null);
    StageDefinition intake = new StageDefinition("intake", "Intake", 0);
    StageDefinition decision = new StageDefinition("decision", "Decision", 1);
    // toConfig removes the "intake" stage
    StageDefinition decisionOnly = new StageDefinition("decision", "Decision", 0);

    CaseTypeConfig fromConfig =
        configWithStages(CT_ID, 1, List.of(open), List.of(nameField), List.of(intake, decision));
    CaseTypeConfig toConfig =
        configWithStages(CT_ID, 2, List.of(open), List.of(nameField), List.of(decisionOnly));

    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":1", fromConfig);
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":2", toConfig);

    // Case currently sits on "intake" stage which is removed in v2
    Case kase =
        new Case(
            CASE_ID,
            CT_ID,
            1,
            "open",
            null,
            Map.of("name", "Alice"),
            null,
            Instant.now(),
            USER_ID,
            Instant.now(),
            0L,
            "intake",
            0);
    caseRepository.save(kase);

    CaseRebaseReport report = service.dryRun(CT_ID, CASE_ID, 2);

    assertThat(report.irreconcilable())
        .anySatisfy(
            item -> {
              assertThat(item.kind()).isEqualTo(IrreconcilableKind.STAGE_REMOVED_WITH_ACTIVE_CASE);
              assertThat(item.currentValue()).isEqualTo("intake");
            });
  }

  // ---- Test: no-op rebase (toVersion == fromVersion) → WKS-API-008 ----

  @Test
  void dryRun_noOpRebase_throwsApi008() {
    versionRegistry.seed(CT_ID, 1, v1Yaml());
    caseRepository.save(makeCase(1, Map.of()));

    assertThatThrownBy(() -> service.dryRun(CT_ID, CASE_ID, 1))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_API_008.wire());
              assertThat(wce.getErrors().get(0).message()).contains("no-op");
            });
  }

  // ---- Test: apply publishes RebaseApplied event with actor/requestId ----

  @Test
  void apply_publishesRebaseAppliedEvent() {
    StatusDefinition open = new StatusDefinition("open", "Open", StatusColor.BLUE);
    FieldDefinition nameField =
        new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, null, null);

    CaseTypeConfig fromConfig = minimalConfig(CT_ID, 1, List.of(open), List.of(nameField));
    CaseTypeConfig toConfig = minimalConfig(CT_ID, 2, List.of(open), List.of(nameField));

    versionRegistry.seed(CT_ID, 1, v1Yaml());
    versionRegistry.seed(CT_ID, 2, v2Yaml());
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":1", fromConfig);
    caseTypeSource.registerConfig("rebase:" + CT_ID + ":2", toConfig);

    caseRepository.save(makeCase(1, Map.of("name", "Alice")));

    service.apply(CT_ID, CASE_ID, 2, "operator@wkspower.local", "req-abc-123");

    assertThat(eventPublisher.events).hasSize(1);
    RebaseApplied event = (RebaseApplied) eventPublisher.events.get(0);
    assertThat(event.caseId()).isEqualTo(CASE_ID);
    assertThat(event.fromVersion()).isEqualTo(1);
    assertThat(event.toVersion()).isEqualTo(2);
    assertThat(event.actor()).isEqualTo("operator@wkspower.local");
    assertThat(event.requestId()).isEqualTo("req-abc-123");
    assertThat(event.forceOverrideReason()).isEqualTo("migration-rebase");
  }

  // ---- Fakes ----

  /** Minimal fake CaseRepository backed by a map. */
  static class FakeCaseRepository implements CaseRepository {
    private final Map<UUID, Case> store = new HashMap<>();

    @Override
    public Case save(Case caseToSave) {
      store.put(caseToSave.id(), caseToSave);
      return caseToSave;
    }

    @Override
    public Optional<Case> findById(UUID id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public Page<CaseSummary> findByCaseType(CaseQuery query, PageRequest pageRequest) {
      throw new UnsupportedOperationException("not needed in unit tests");
    }

    @Override
    public Map<UUID, Map<String, Object>> findDataByIds(
        Collection<UUID> ids, Set<String> projectedFieldIds) {
      throw new UnsupportedOperationException("not needed in unit tests");
    }

    /** Test hook — when set, the next updateCaseTypeVersion call sees the bumped version. */
    boolean simulateConcurrentBumpOnNextUpdate = false;

    @Override
    public int updateCaseTypeVersion(UUID caseId, int toCaseTypeVersion, long expectedVersion) {
      if (simulateConcurrentBumpOnNextUpdate) {
        bumpStoredVersion(caseId);
        simulateConcurrentBumpOnNextUpdate = false;
      }
      Case existing = store.get(caseId);
      if (existing == null || existing.version() != expectedVersion) {
        return 0;
      }
      Case bumped =
          new Case(
              existing.id(),
              existing.caseTypeId(),
              toCaseTypeVersion,
              existing.status(),
              existing.assignee(),
              existing.data(),
              existing.processInstanceId(),
              existing.createdAt(),
              existing.createdBy(),
              existing.updatedAt(),
              existing.version() + 1,
              existing.currentStageId(),
              existing.currentStageOrdinal());
      store.put(caseId, bumped);
      return 1;
    }

    @Override
    public int updateCaseTypeVersionAndStage(
        UUID caseId,
        int toCaseTypeVersion,
        String toStageId,
        int toStageOrdinal,
        long expectedVersion) {
      if (simulateConcurrentBumpOnNextUpdate) {
        bumpStoredVersion(caseId);
        simulateConcurrentBumpOnNextUpdate = false;
      }
      Case existing = store.get(caseId);
      if (existing == null || existing.version() != expectedVersion) {
        return 0;
      }
      Case bumped =
          new Case(
              existing.id(),
              existing.caseTypeId(),
              toCaseTypeVersion,
              existing.status(),
              existing.assignee(),
              existing.data(),
              existing.processInstanceId(),
              existing.createdAt(),
              existing.createdBy(),
              existing.updatedAt(),
              existing.version() + 1,
              toStageId,
              toStageOrdinal);
      store.put(caseId, bumped);
      return 1;
    }

    /** Test helper — simulate a concurrent transaction bumping the @Version column. */
    void bumpStoredVersion(UUID caseId) {
      Case existing = store.get(caseId);
      if (existing == null) {
        return;
      }
      store.put(
          caseId,
          new Case(
              existing.id(),
              existing.caseTypeId(),
              existing.caseTypeVersion(),
              existing.status(),
              existing.assignee(),
              existing.data(),
              existing.processInstanceId(),
              existing.createdAt(),
              existing.createdBy(),
              existing.updatedAt(),
              existing.version() + 1,
              existing.currentStageId(),
              existing.currentStageOrdinal()));
    }
  }

  /** Recording EventPublisher for unit-test assertions on emitted events. */
  static class RecordingEventPublisher implements EventPublisher {
    final List<Object> events = new ArrayList<>();

    @Override
    public void publish(Object event) {
      events.add(event);
    }
  }

  /** Minimal fake CaseTypeSource that returns pre-registered configs. */
  static class FakeCaseTypeSource implements CaseTypeSource {
    private final Map<String, CaseTypeConfig> configs = new HashMap<>();

    void registerConfig(String sourceName, CaseTypeConfig config) {
      configs.put(sourceName, config);
    }

    @Override
    public ValidationResult load(Path file) {
      throw new UnsupportedOperationException("not needed in unit tests");
    }

    @Override
    public ValidationResult loadBytes(String source, byte[] bytes, Map<String, byte[]> bpmnByName) {
      CaseTypeConfig config = configs.get(source);
      if (config != null) {
        return ValidationResult.ok(config);
      }
      return ValidationResult.invalid(
          List.of(
              com.wkspower.platform.domain.exception.ErrorDetail.of(
                  "WKS-CFG-099", "no config registered for source: " + source)));
    }
  }
}
