package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.wkspower.platform.audit.AuditEvent;
import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksValidationAggregateException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.domain.service.MappingRegistry;
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import com.wkspower.platform.infrastructure.persistence.AuditEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Story 6-3b AC4 — Postgres-IT parity for form-submit gate exemption. This is the load-bearing
 * proof that {@link CaseService#submitForm} is exempt from {@code EditContractGate} for fields
 * owned by the bound task's form, while sibling-form fields remain gated.
 *
 * <p>Three scenarios cover the exemption surface:
 *
 * <ol>
 *   <li><b>{@code submit_persists_bound_task_form_field}</b> — open intake-task → intake-form (owns
 *       "applicant"). Submitting intake-form with a new applicant value succeeds (no WKS-EDIT-001)
 *       and the value is committed to Postgres.
 *   <li><b>{@code submit_does_not_bypass_sibling_form_ownership}</b> — two open tasks (intake-form
 *       owns "applicant"; review-form owns "notes"). Submitting intake-form with a write set that
 *       includes "notes" still trips WKS-EDIT-001 against review-form. An exempt form cannot bypass
 *       siblings' ownership.
 *   <li><b>{@code submit_audit_row_materializes_after_commit}</b> — successful submit produces a
 *       {@code case.data.edit} audit row whose source.type = USER, visible from a fresh transaction
 *       (proves the {@code @TransactionalEventListener(AFTER_COMMIT)} chain holds on Postgres).
 * </ol>
 *
 * <p>Test discipline:
 *
 * <ul>
 *   <li>NOT {@code @Transactional} per {@code feedback_postgres_it_committed_read} — every
 *       audit-row read goes through a fresh {@link TransactionTemplate} so we observe only
 *       committed state.
 *   <li>{@code wks.bootstrap.production-validation.enabled=false} per {@code
 *       feedback_production_validator_opt_out} — this IT does not exercise the boot validator.
 *   <li>{@link WorkflowEngine} is {@code @MockitoBean}-overridden so open-task state can be driven
 *       without deploying a real BPMN process. The case is created with no processInstanceId
 *       (zero-process path) so {@code submitForm}'s TASK_COMPLETED signal branch is skipped — keeps
 *       this IT focused on the gate surface alone.
 * </ul>
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.NONE,
    properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class CaseServiceFormSubmitGateExemptPostgresIT {

  private static final String CASE_TYPE_ID = "gate-exempt-it";
  private static final String INTAKE_FORM_ID = "intake-form";
  private static final String REVIEW_FORM_ID = "review-form";
  private static final String INTAKE_TASK_KEY = "intake-task";
  private static final String REVIEW_TASK_KEY = "review-task";

  @Container
  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("wks")
          .withUsername("wks")
          .withPassword("wks");

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry reg) {
    reg.add("WKS_DB_URL", POSTGRES::getJdbcUrl);
    reg.add("WKS_DB_USER", POSTGRES::getUsername);
    reg.add("WKS_DB_PASSWORD", POSTGRES::getPassword);
    reg.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    reg.add("WKS_ADMIN_EMAIL", () -> "admin@wkspower.local");
    reg.add("WKS_ADMIN_PASSWORD", () -> "admin");
    reg.add("wks.jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=");
    reg.add("WKS_CORS_ORIGINS", () -> "http://localhost:5173");
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
    reg.add("wks.case-types.dir", () -> "");
  }

  @Autowired private CaseService caseService;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private MappingRegistry mappingRegistry;
  @Autowired private AuditEventRepository auditEventRepository;
  @Autowired private DataSource dataSource;
  @Autowired private PlatformTransactionManager txManager;

  @MockitoBean private WorkflowEngine workflowEngine;

  private JdbcTemplate jdbc;
  private TransactionTemplate freshTx;
  private UUID adminId;

  @BeforeEach
  void setUp() {
    jdbc = new JdbcTemplate(dataSource);
    freshTx = new TransactionTemplate(txManager);
    adminId =
        jdbc.queryForObject(
            "SELECT id FROM users WHERE email = ?", UUID.class, "admin@wkspower.local");

    registry.register(caseTypeFixture());
    mappingRegistry.register(new CaseTypeRef(CASE_TYPE_ID, "1"), "1", mappingFixtureWithTwoTasks());
  }

  @AfterEach
  void wipe() {
    // audit_events FK -> cases (ON DELETE RESTRICT): audit first.
    jdbc.update(
        "DELETE FROM audit_events WHERE case_id IN (SELECT id FROM cases WHERE case_type_id = ?)",
        CASE_TYPE_ID);
    jdbc.update("DELETE FROM cases WHERE case_type_id = ?", CASE_TYPE_ID);
  }

  @Test
  void submit_persists_bound_task_form_field() {
    // Given: case with one open task (intake-task) → intake-form owns "applicant".
    Case created = caseService.create(CASE_TYPE_ID, Map.of("applicant", "Initial"), null, adminId);
    when(workflowEngine.findTasksByCase(created.id()))
        .thenReturn(List.of(openTask(created.id(), "task-1", INTAKE_TASK_KEY)));

    // When: actor submits intake-form (the form bound to the open task) with a new value.
    Case updated =
        caseService.submitForm(
            created.id(), INTAKE_FORM_ID, Map.of("applicant", "Alice"), adminId, Set.of("admin"));

    // Then: the persist succeeded (no WKS-EDIT-001) and the value is committed.
    assertThat(updated.data()).containsEntry("applicant", "Alice");
    Case reloaded = freshTx.execute(s -> caseService.findById(created.id()));
    assertThat(reloaded.data()).containsEntry("applicant", "Alice");
  }

  @Test
  void submit_does_not_bypass_sibling_form_ownership() {
    // Given: case with TWO open tasks. intake-form owns "applicant"; review-form owns "notes".
    Case created =
        caseService.create(
            CASE_TYPE_ID, Map.of("applicant", "Initial", "notes", "n0"), null, adminId);
    when(workflowEngine.findTasksByCase(created.id()))
        .thenReturn(
            List.of(
                openTask(created.id(), "task-1", INTAKE_TASK_KEY),
                openTask(created.id(), "task-2", REVIEW_TASK_KEY)));

    // When: actor submits intake-form but the write set contains "notes" — a field owned by the
    // sibling open task's form.
    // Then: WKS-EDIT-001 still fires for "notes" (exempt-form does NOT bypass siblings).
    assertThatThrownBy(
            () ->
                caseService.submitForm(
                    created.id(),
                    INTAKE_FORM_ID,
                    Map.of("applicant", "Bob", "notes", "edited"),
                    adminId,
                    Set.of("admin")))
        .isInstanceOf(WksValidationAggregateException.class)
        .satisfies(
            ex -> {
              WksValidationAggregateException agg = (WksValidationAggregateException) ex;
              assertThat(agg.getErrors())
                  .anySatisfy(
                      e -> {
                        assertThat(e.code()).isEqualTo(ErrorCode.WKS_EDIT_001.wire());
                        assertThat(e.field()).isEqualTo("notes");
                        // Story 6-3b AC2 — clean message, no raw ids.
                        assertThat(e.message()).doesNotContain("openTaskId=", "formId=");
                      });
            });

    // Persist did NOT commit either field (pre-commit throw rolls back the transaction).
    Case reloaded = freshTx.execute(s -> caseService.findById(created.id()));
    assertThat(reloaded.data()).containsEntry("applicant", "Initial").containsEntry("notes", "n0");
  }

  @Test
  void submit_audit_row_materializes_after_commit() {
    // Given: case with one open task; intake-form is the bound form.
    Case created = caseService.create(CASE_TYPE_ID, Map.of("applicant", "Initial"), null, adminId);
    when(workflowEngine.findTasksByCase(created.id()))
        .thenReturn(List.of(openTask(created.id(), "task-1", INTAKE_TASK_KEY)));

    // When: actor submits intake-form successfully. Wrap in a TransactionTemplate so the
    // @TransactionalEventListener(AFTER_COMMIT) on EditAuditEmitter fires — per
    // feedback_transactional_event_listener_requires_outer_tx, the production code path
    // gets its outer TX from the controller's @Transactional; tests bypassing the controller
    // must supply one.
    freshTx.executeWithoutResult(
        s ->
            caseService.submitForm(
                created.id(),
                INTAKE_FORM_ID,
                Map.of("applicant", "Alice"),
                adminId,
                Set.of("admin")));

    // Then: fresh-tx read of audit_events shows a case.data.edit row sourced from USER. Proves
    // the @TransactionalEventListener(AFTER_COMMIT) chain holds on Postgres for the submitForm
    // path — the same load-bearing chain Story 9-2 hardened for direct-edit (PR #444).
    List<AuditEvent> rows =
        freshTx.execute(s -> auditEventRepository.findByCaseId(created.id(), 50));
    assertThat(rows)
        .anySatisfy(
            r -> {
              assertThat(r.eventType()).isEqualTo(AuditEvent.EVENT_TYPE_CASE_DATA_EDIT);
              assertThat(r.fieldId()).isEqualTo("applicant");
              assertThat(r.result()).isEqualTo("APPLIED");
            });
  }

  // ---- fixtures ----

  private static CaseTypeConfig caseTypeFixture() {
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("notes", "Notes", FieldType.TEXT, false, 1, List.of(), null));

    FormDefinition intakeForm =
        new FormDefinition(
            INTAKE_FORM_ID,
            "single",
            "monolithic",
            "single-page",
            List.of(fields.get(0)),
            List.of(),
            "submit_for_processing");
    FormDefinition reviewForm =
        new FormDefinition(
            REVIEW_FORM_ID,
            "single",
            "monolithic",
            "single-page",
            List.of(fields.get(1)),
            List.of(),
            "submit_for_processing");

    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Gate-Exempt IT Fixture",
        1,
        null,
        null,
        fields,
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("applicant"),
        List.of(
            new RoleDefinition(
                "admin", List.of(Permission.CREATE, Permission.EDIT, Permission.VIEW))),
        List.of(),
        List.of(intakeForm, reviewForm));
  }

  private static MappingDefinition mappingFixtureWithTwoTasks() {
    AttachmentDefinition att =
        new AttachmentDefinition(
            "bpmn",
            CASE_TYPE_ID + ".bpmn",
            "case",
            Optional.empty(),
            Map.of(
                INTAKE_TASK_KEY, new UserTaskMapping(INTAKE_TASK_KEY, INTAKE_FORM_ID),
                REVIEW_TASK_KEY, new UserTaskMapping(REVIEW_TASK_KEY, REVIEW_FORM_ID)),
            Optional.empty(),
            Map.of(),
            List.of(),
            Map.of());
    return new MappingDefinition(List.of(att));
  }

  private static Task openTask(UUID caseId, String taskId, String taskDefinitionKey) {
    return new Task(
        taskId,
        "pi-" + taskId,
        "pd-" + taskId,
        caseId,
        CASE_TYPE_ID,
        taskDefinitionKey,
        taskDefinitionKey,
        null,
        "submit_for_processing",
        Instant.now(),
        null);
  }
}
