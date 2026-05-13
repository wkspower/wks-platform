package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksValidationException;
import com.wkspower.platform.domain.model.CrossCaseTaskListResult;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Pure-Java enumeration of {@code TaskService} branches. No Spring context, no engine — every
 * collaborator is a hand-rolled stub.
 */
class TaskServiceTest {

  private static final UUID ACTOR = UUID.randomUUID();
  private static final UUID CASE = UUID.randomUUID();
  private static final Instant NOW = Instant.parse("2026-04-26T10:00:00Z");

  @Test
  void findByIdReturnsTaskWhenEngineFindsIt() {
    Task task = sampleTask("t1");
    StubEngine engine = new StubEngine();
    engine.tasks.put("t1", task);

    Task found = new TaskService(engine).findById("t1");

    assertThat(found.id()).isEqualTo("t1");
    assertThat(found.archetype()).isEqualTo("draft_section");
  }

  @Test
  void findByIdThrows404WhenEngineEmpty() {
    assertThatThrownBy(() -> new TaskService(new StubEngine()).findById("missing"))
        .isInstanceOf(WksNotFoundException.class);
  }

  @Test
  void completeRetainsSnapshotAndCallsEngineComplete() {
    Task task = sampleTask("t1");
    StubEngine engine = new StubEngine();
    engine.tasks.put("t1", task);

    Task returned = new TaskService(engine).complete("t1", Map.of("k", "v"), ACTOR);

    assertThat(returned.id()).isEqualTo("t1");
    assertThat(returned.archetype()).isEqualTo("draft_section");
    assertThat(engine.completed).hasSize(1);
    assertThat(engine.completed.get(0).id).isEqualTo("t1");
    assertThat(engine.completed.get(0).variables).containsEntry("k", "v");
  }

  @Test
  void completeThrows404WhenSnapshotMissing() {
    assertThatThrownBy(() -> new TaskService(new StubEngine()).complete("none", Map.of(), ACTOR))
        .isInstanceOf(WksNotFoundException.class);
  }

  @Test
  void completeNullVariablesAreCoercedToEmpty() {
    Task task = sampleTask("t1");
    StubEngine engine = new StubEngine();
    engine.tasks.put("t1", task);

    new TaskService(engine).complete("t1", null, ACTOR);

    assertThat(engine.completed.get(0).variables).isEmpty();
  }

  @Test
  void completeRejectsCompletionByNonAssignee() {
    // Story 2.4 review — AC2 assignee enforcement. A task assigned to user X cannot be completed
    // by user Y even if Y holds the `transition` verb.
    UUID otherUser = UUID.randomUUID();
    Task assignedToOther =
        new Task(
            "t1",
            "pi-1",
            "pd-1",
            CASE,
            "loan-application",
            "draft",
            "Draft",
            otherUser,
            "draft_section",
            NOW,
            null);
    StubEngine engine = new StubEngine();
    engine.tasks.put("t1", assignedToOther);

    assertThatThrownBy(() -> new TaskService(engine).complete("t1", Map.of(), ACTOR))
        .isInstanceOf(WksConflictException.class)
        .hasMessageContaining("assigned to another user");
    assertThat(engine.completed)
        .as("engine.complete must NOT be called when assignee mismatch is detected")
        .isEmpty();
  }

  @Test
  void completeAllowsAssigneeToComplete() {
    // Same actor as assignee — happy path of the AC2 contract.
    Task assignedToActor =
        new Task(
            "t1",
            "pi-1",
            "pd-1",
            CASE,
            "loan-application",
            "draft",
            "Draft",
            ACTOR,
            "draft_section",
            NOW,
            null);
    StubEngine engine = new StubEngine();
    engine.tasks.put("t1", assignedToActor);

    new TaskService(engine).complete("t1", Map.of(), ACTOR);

    assertThat(engine.completed).hasSize(1);
  }

  @Test
  void completeRejectsReservedProcessVariables() {
    // Story 2.4 review — variable-injection guard. caseId/caseTypeId/taskAssignee/caseStatus
    // would let a caller corrupt another case's status row via the listener.
    Task task = sampleTask("t1");
    StubEngine engine = new StubEngine();
    engine.tasks.put("t1", task);

    assertThatThrownBy(
            () ->
                new TaskService(engine)
                    .complete("t1", Map.of("caseId", UUID.randomUUID().toString()), ACTOR))
        .isInstanceOf(WksValidationException.class)
        .hasMessageContaining("reserved");
    assertThat(engine.completed)
        .as("engine.complete must NOT be called when reserved variables are supplied")
        .isEmpty();
  }

  @Test
  void completePropagatesEngineConflict() {
    Task task = sampleTask("t1");
    StubEngine engine = new StubEngine();
    engine.tasks.put("t1", task);
    engine.completeFailure = new WksConflictException("already completed");

    assertThatThrownBy(() -> new TaskService(engine).complete("t1", Map.of(), ACTOR))
        .isInstanceOf(WksConflictException.class);
  }

  @Test
  void claimReturnsTaskAfterEngineClaim() {
    StubEngine engine = new StubEngine();
    engine.tasks.put("t1", sampleTask("t1"));
    engine.afterClaim =
        new Task(
            "t1",
            "pi-1",
            "pd-1",
            CASE,
            "loan-application",
            "draft",
            "Draft section",
            ACTOR,
            "draft_section",
            NOW,
            null);

    Task claimed = new TaskService(engine).claim("t1", ACTOR);

    assertThat(claimed.assignee()).isEqualTo(ACTOR);
    assertThat(engine.claimed).hasSize(1);
    assertThat(engine.claimed.get(0).userId).isEqualTo(ACTOR);
  }

  @Test
  void claimPropagatesEngineConflict() {
    StubEngine engine = new StubEngine();
    engine.claimFailure = new WksConflictException("already claimed");

    assertThatThrownBy(() -> new TaskService(engine).claim("t1", ACTOR))
        .isInstanceOf(WksConflictException.class);
  }

  @Test
  void claimThrows404WhenPostClaimSnapshotMissing() {
    StubEngine engine = new StubEngine();
    // claim succeeds, but the post-claim find returns empty (engine race)
    engine.afterClaimMissing = true;

    assertThatThrownBy(() -> new TaskService(engine).claim("t1", ACTOR))
        .isInstanceOf(WksNotFoundException.class);
  }

  // ---- Story 13-1 listAcrossCases ---------------------------------------

  @Test
  void listAcrossCasesShortCircuitsOnEmptyPermittedSet() {
    StubEngine engine = new StubEngine();
    CrossCaseTaskListResult result = new TaskService(engine).listAcrossCases(Set.of(), 500);

    assertThat(result.tasks()).isEmpty();
    assertThat(result.truncated()).isFalse();
    assertThat(engine.listCalls).isEqualTo(0);
  }

  @Test
  void listAcrossCasesShortCircuitsOnNonPositiveLimit() {
    StubEngine engine = new StubEngine();
    CrossCaseTaskListResult result = new TaskService(engine).listAcrossCases(Set.of("A"), 0);

    assertThat(result.tasks()).isEmpty();
    assertThat(engine.listCalls).isEqualTo(0);
  }

  @Test
  void listAcrossCasesDelegatesPermittedSetToEngine() {
    StubEngine engine = new StubEngine();
    engine.listResult = new CrossCaseTaskListResult(List.of(sampleTask("t1")), false);

    CrossCaseTaskListResult result = new TaskService(engine).listAcrossCases(Set.of("A", "B"), 500);

    assertThat(engine.listCalls).isEqualTo(1);
    assertThat(engine.lastPermittedCaseTypeIds).containsExactlyInAnyOrder("A", "B");
    assertThat(engine.lastLimit).isEqualTo(500);
    assertThat(result.tasks()).hasSize(1);
  }

  // ---- helpers -----------------------------------------------------------

  private static Task sampleTask(String id) {
    return new Task(
        id,
        "pi-1",
        "pd-1",
        CASE,
        "loan-application",
        "draft",
        "Draft section",
        null,
        "draft_section",
        NOW,
        null);
  }

  private static final class CompleteCall {
    final String id;
    final Map<String, Object> variables;

    CompleteCall(String id, Map<String, Object> variables) {
      this.id = id;
      this.variables = variables;
    }
  }

  private static final class ClaimCall {
    final String id;
    final UUID userId;

    ClaimCall(String id, UUID userId) {
      this.id = id;
      this.userId = userId;
    }
  }

  private static final class StubEngine implements WorkflowEngine {
    final Map<String, Task> tasks = new HashMap<>();
    final List<CompleteCall> completed = new ArrayList<>();
    final List<ClaimCall> claimed = new ArrayList<>();
    RuntimeException completeFailure;
    RuntimeException claimFailure;
    Task afterClaim;
    boolean afterClaimMissing;
    int listCalls;
    Set<String> lastPermittedCaseTypeIds;
    int lastLimit;
    CrossCaseTaskListResult listResult = CrossCaseTaskListResult.empty();

    @Override
    public CrossCaseTaskListResult listPendingTasks(Set<String> permittedCaseTypeIds, int limit) {
      listCalls++;
      lastPermittedCaseTypeIds = permittedCaseTypeIds;
      lastLimit = limit;
      return listResult;
    }

    @Override
    public DeploymentResult deploy(DeploymentRequest request) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<DeploymentInfo> latestDeployment(String key) {
      return Optional.empty();
    }

    @Override
    public String startProcessInstance(String key, Map<String, Object> variables) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Task> findTask(String taskId) {
      if (afterClaim != null && !claimed.isEmpty()) {
        return Optional.of(afterClaim);
      }
      if (afterClaimMissing && !claimed.isEmpty()) {
        return Optional.empty();
      }
      return Optional.ofNullable(tasks.get(taskId));
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
      if (completeFailure != null) throw completeFailure;
      completed.add(new CompleteCall(taskId, variables));
    }

    @Override
    public void claimTask(String taskId, UUID userId) {
      if (claimFailure != null) throw claimFailure;
      claimed.add(new ClaimCall(taskId, userId));
    }

    @Override
    public void signalTransition(
        String processInstanceId, String action, Map<String, Object> variables) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Task> findTasksByCase(UUID caseId) {
      return List.of();
    }

    @Override
    public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
      return null;
    }
  }
}
