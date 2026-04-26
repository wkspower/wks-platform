package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.event.CaseCreated;
import com.wkspower.platform.domain.event.CaseUpdated;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksValidationAggregateException;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.port.CaseDataValidator;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.ProcessDefinitionKeyResolver;
import com.wkspower.platform.domain.port.WorkflowEngine;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service orchestrating case CRUD. Framework-free — no Spring, no JPA, no Jackson on the
 * import list — collaborators are reached via the six ports declared in {@code domain/port/}.
 *
 * <p>The {@code create} ordering is engine-first / DB-second by design (see Story 2.3 Dev Notes
 * §processInstanceId ordering). If the engine succeeds and the DB persist fails, the engine has a
 * dangling process instance — accepted partial state for Phase 0; Phase 1 wraps in an outbox.
 */
public class CaseService {

  private final CaseRepository caseRepository;
  private final CaseTypeReader caseTypeReader;
  private final CaseDataValidator caseDataValidator;
  private final WorkflowEngine workflowEngine;
  private final ProcessDefinitionKeyResolver processKeyResolver;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public CaseService(
      CaseRepository caseRepository,
      CaseTypeReader caseTypeReader,
      CaseDataValidator caseDataValidator,
      WorkflowEngine workflowEngine,
      ProcessDefinitionKeyResolver processKeyResolver,
      EventPublisher eventPublisher,
      Clock clock) {
    this.caseRepository = Objects.requireNonNull(caseRepository, "caseRepository");
    this.caseTypeReader = Objects.requireNonNull(caseTypeReader, "caseTypeReader");
    this.caseDataValidator = Objects.requireNonNull(caseDataValidator, "caseDataValidator");
    this.workflowEngine = Objects.requireNonNull(workflowEngine, "workflowEngine");
    this.processKeyResolver = Objects.requireNonNull(processKeyResolver, "processKeyResolver");
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  /**
   * Create a new case. Steps follow Story 2.3 AC4: load case type → validate data → start BPMN
   * process instance → persist row → publish {@link CaseCreated}.
   */
  public Case create(String caseTypeId, Map<String, Object> data, UUID assignee, UUID actorId) {
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Objects.requireNonNull(actorId, "actorId");

    CaseTypeConfig caseType = requireCaseType(caseTypeId);
    Map<String, Object> safeData = data == null ? Map.of() : Map.copyOf(data);

    List<ErrorDetail> violations = caseDataValidator.validate(caseType, safeData);
    if (!violations.isEmpty()) {
      throw new WksValidationAggregateException("Case data failed validation", violations);
    }

    UUID caseId = UUID.randomUUID();
    String initialStatus = initialStatus(caseType);

    String processDefinitionKey =
        processKeyResolver
            .resolve(caseType.id())
            .orElseThrow(
                () ->
                    new WksWorkflowEngineException(
                        "No deployed BPMN process definition for case type "
                            + caseType.id()
                            + " — case create requires a deployed workflow"));
    String processInstanceId =
        workflowEngine.startProcessInstance(
            processDefinitionKey, Map.of("caseId", caseId.toString()));

    Instant now = clock.now();
    Case toPersist =
        new Case(
            caseId,
            caseType.id(),
            caseType.version(),
            initialStatus,
            assignee,
            safeData,
            processInstanceId,
            now,
            actorId,
            now,
            0L);
    Case persisted = caseRepository.save(toPersist);

    eventPublisher.publish(
        new CaseCreated(persisted.id(), caseType.id(), caseType.version(), actorId, now));
    return persisted;
  }

  /**
   * Update an existing case's {@code data}. The expected {@code version} is checked via the JPA
   * {@code @Version} optimistic lock — mismatch surfaces as {@link WksConflictException}.
   */
  public Case update(UUID caseId, Map<String, Object> newData, long expectedVersion, UUID actorId) {
    Objects.requireNonNull(caseId, "caseId");
    Objects.requireNonNull(actorId, "actorId");

    Case existing =
        caseRepository
            .findById(caseId)
            .orElseThrow(() -> new WksNotFoundException("Case " + caseId + " not found"));
    if (existing.version() != expectedVersion) {
      throw new WksConflictException(
          "Case " + caseId + " was modified by another transaction; reload and retry");
    }

    CaseTypeConfig caseType = requireCaseType(existing.caseTypeId());
    Map<String, Object> safeData = newData == null ? Map.of() : Map.copyOf(newData);

    List<ErrorDetail> violations = caseDataValidator.validate(caseType, safeData);
    if (!violations.isEmpty()) {
      throw new WksValidationAggregateException("Case data failed validation", violations);
    }

    Set<String> changedFieldIds = diffFieldIds(existing.data(), safeData);
    Instant now = clock.now();
    Case updated =
        new Case(
            existing.id(),
            existing.caseTypeId(),
            existing.caseTypeVersion(),
            existing.status(),
            existing.assignee(),
            safeData,
            existing.processInstanceId(),
            existing.createdAt(),
            existing.createdBy(),
            now,
            existing.version());

    Case persisted = caseRepository.save(updated);
    eventPublisher.publish(new CaseUpdated(persisted.id(), actorId, now, changedFieldIds));
    return persisted;
  }

  /** Lookup by id; 404 if not found. */
  public Case findById(UUID caseId) {
    return caseRepository
        .findById(caseId)
        .orElseThrow(() -> new WksNotFoundException("Case " + caseId + " not found"));
  }

  /**
   * Paginated list of case summaries scoped to a case type. After fetching the lightweight rows we
   * enrich {@code CaseSummary.fields} from {@code caseType.listColumns} via {@code findDataByIds}
   * (Story 2.3 D4) — keeps the wide JSON column out of the main projection query while still
   * delivering the listColumns values the wire contract promises.
   */
  public Page<CaseSummary> list(CaseQuery query, PageRequest pageRequest) {
    Page<CaseSummary> page = caseRepository.findByCaseType(query, pageRequest);
    if (page.content().isEmpty()) {
      return page;
    }
    CaseTypeConfig caseType = requireCaseType(query.caseTypeId());
    Set<String> projectedFieldIds = Set.copyOf(caseType.listColumns());
    if (projectedFieldIds.isEmpty()) {
      return page;
    }
    List<UUID> ids = page.content().stream().map(CaseSummary::id).toList();
    Map<UUID, Map<String, Object>> dataById = caseRepository.findDataByIds(ids, projectedFieldIds);
    List<CaseSummary> enriched = new ArrayList<>(page.content().size());
    for (CaseSummary s : page.content()) {
      enriched.add(
          new CaseSummary(
              s.id(),
              s.caseTypeId(),
              s.status(),
              s.assignee(),
              s.createdAt(),
              s.updatedAt(),
              dataById.getOrDefault(s.id(), Map.of())));
    }
    return new Page<>(enriched, page.total(), page.page(), page.size());
  }

  /**
   * Locate the case type — 404 if it does not exist. Used on create and update; the controller has
   * already returned 403 on missing permission verbs by the time this fires.
   */
  public CaseTypeConfig requireCaseType(String caseTypeId) {
    return caseTypeReader
        .find(caseTypeId)
        .orElseThrow(() -> new WksNotFoundException("Case type " + caseTypeId + " not found"));
  }

  /**
   * Initial status comes from the YAML — {@code statuses[0].id} per Story 2.3 Dev Notes §Initial
   * status semantics. Story 2.4's BPMN listener will keep it in sync with engine state thereafter.
   */
  private static String initialStatus(CaseTypeConfig caseType) {
    return caseType.statuses().stream()
        .map(StatusDefinition::id)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Case type "
                        + caseType.id()
                        + " has no statuses — should have been rejected by"
                        + " the validator"));
  }

  /**
   * Set of field ids whose value differs between {@code oldData} and {@code newData}. A field
   * present on one side but absent from the other counts as changed.
   */
  static Set<String> diffFieldIds(Map<String, Object> oldData, Map<String, Object> newData) {
    Set<String> changed = new HashSet<>();
    Set<String> allKeys = new HashSet<>(oldData.keySet());
    allKeys.addAll(newData.keySet());
    for (String key : allKeys) {
      Object o = oldData.get(key);
      Object n = newData.get(key);
      if (!Objects.equals(o, n)) {
        changed.add(key);
      }
    }
    return changed;
  }
}
