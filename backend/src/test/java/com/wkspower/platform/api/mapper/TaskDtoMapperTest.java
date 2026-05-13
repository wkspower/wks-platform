package com.wkspower.platform.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.api.dto.response.TaskDto;
import com.wkspower.platform.domain.model.Task;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

/**
 * Story 2-6-1 — unit tests for {@link TaskDtoMapper#toDtos(List, BiFunction, BiFunction)}'s
 * {@code formId} projection. Mirrors the existing {@code actionLabelLookup} resilience contract:
 * an unloadable mapping must not poison sibling tasks.
 */
class TaskDtoMapperTest {

  private static final Instant NOW = Instant.parse("2026-05-13T10:00:00Z");
  private static final UUID CASE_ID = UUID.randomUUID();

  private static Task task(String id, String pdId, String taskDefKey, String name) {
    return new Task(id, "pi-1", pdId, CASE_ID, "loan", taskDefKey, name, null, "draft_section",
        NOW, null);
  }

  @Test
  void populatesFormIdWhenLookupReturnsValue() {
    List<TaskDto> dtos =
        TaskDtoMapper.toDtos(
            List.of(task("t1", "pd-1", "draft", "Draft")),
            (pd, k) -> "Draft",
            (pd, k) -> "loan-form");

    assertThat(dtos).hasSize(1);
    assertThat(dtos.get(0).formId()).isEqualTo("loan-form");
  }

  @Test
  void formIdIsNullWhenLookupReturnsNull() {
    List<TaskDto> dtos =
        TaskDtoMapper.toDtos(
            List.of(task("t1", "pd-1", "draft", "Draft")),
            (pd, k) -> "Draft",
            (pd, k) -> null);

    assertThat(dtos.get(0).formId()).isNull();
  }

  @Test
  void formIdIsNullWhenLookupReturnsBlank() {
    List<TaskDto> dtos =
        TaskDtoMapper.toDtos(
            List.of(task("t1", "pd-1", "draft", "Draft")),
            (pd, k) -> "Draft",
            (pd, k) -> "   ");

    assertThat(dtos.get(0).formId()).isNull();
  }

  @Test
  void oneUnresolvableMappingDoesNotPoisonSiblingTasks() {
    // First task's lookup throws; second task's lookup succeeds. List must still render and the
    // second task must keep its formId — mirrors the actionLabel cache resilience contract.
    BiFunction<String, String, String> formIdLookup =
        (pd, k) -> {
          if ("draft".equals(k)) {
            throw new RuntimeException("BPMN unloadable");
          }
          return "review-form";
        };

    List<TaskDto> dtos =
        TaskDtoMapper.toDtos(
            List.of(
                task("t1", "pd-1", "draft", "Draft"),
                task("t2", "pd-1", "review", "Review")),
            (pd, k) -> k,
            formIdLookup);

    assertThat(dtos).hasSize(2);
    assertThat(dtos.get(0).formId()).isNull();
    assertThat(dtos.get(1).formId()).isEqualTo("review-form");
  }

  @Test
  void formIdLookupCachedPerProcessDefAndTaskDefKey() {
    AtomicInteger calls = new AtomicInteger();
    BiFunction<String, String, String> formIdLookup =
        (pd, k) -> {
          calls.incrementAndGet();
          return "loan-form";
        };

    List<TaskDto> dtos =
        TaskDtoMapper.toDtos(
            List.of(
                task("t1", "pd-1", "draft", "Draft"),
                task("t2", "pd-1", "draft", "Draft"),
                task("t3", "pd-1", "draft", "Draft")),
            (pd, k) -> "Draft",
            formIdLookup);

    assertThat(dtos).extracting(TaskDto::formId).containsOnly("loan-form");
    // Three identical (pd, k) pairs but the lookup is consulted exactly once.
    assertThat(calls.get()).isEqualTo(1);
  }

  @Test
  void formIdNullCachedSoMissingMappingNotRequeried() {
    AtomicInteger calls = new AtomicInteger();
    BiFunction<String, String, String> formIdLookup =
        (pd, k) -> {
          calls.incrementAndGet();
          return null;
        };

    TaskDtoMapper.toDtos(
        List.of(
            task("t1", "pd-1", "draft", "Draft"),
            task("t2", "pd-1", "draft", "Draft")),
        (pd, k) -> "Draft",
        formIdLookup);

    // Even though the lookup returned null, the second call must hit the cache (sentinel),
    // not re-query.
    assertThat(calls.get()).isEqualTo(1);
  }
}
