package com.wkspower.platform.api.mapper;

import com.wkspower.platform.api.dto.response.TaskDto;
import com.wkspower.platform.domain.model.Task;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Story 2.8 AC1 — domain {@link Task} → {@link TaskDto} mapping with per-request action-label
 * caching keyed by {@code (processDefinitionId, taskDefinitionKey)}. The cache lifetime is the
 * single mapper invocation; cross-request caching ships in a later perf pass (story Dev Notes).
 */
public final class TaskDtoMapper {

  private TaskDtoMapper() {}

  /**
   * Map a list of tasks to DTOs, resolving each task's {@code actionLabel} via {@code
   * actionLabelLookup}. The lookup is invoked at most once per {@code (processDefinitionId,
   * taskDefinitionKey)} pair within this call.
   */
  public static List<TaskDto> toDtos(
      List<Task> tasks, BiFunction<String, String, String> actionLabelLookup) {
    Map<String, String> cache = new HashMap<>();
    return tasks.stream().map(t -> toDto(t, actionLabelLookup, cache)).toList();
  }

  private static TaskDto toDto(
      Task task, BiFunction<String, String, String> lookup, Map<String, String> cache) {
    String actionLabel = resolveActionLabel(task, lookup, cache);
    return new TaskDto(
        task.id(),
        task.processInstanceId(),
        task.caseId(),
        task.caseTypeId(),
        task.taskDefinitionKey(),
        task.name(),
        task.assignee(),
        task.archetype(),
        actionLabel,
        task.createdAt(),
        task.dueAt());
  }

  private static String resolveActionLabel(
      Task task, BiFunction<String, String, String> lookup, Map<String, String> cache) {
    if (task.processDefinitionId() == null || task.taskDefinitionKey() == null) {
      // Fall back to task.name when we cannot read the BPMN — never leave actionLabel blank.
      return task.name();
    }
    String key = task.processDefinitionId() + "::" + task.taskDefinitionKey();
    String cached = cache.get(key);
    if (cached != null) {
      return cached;
    }
    String resolved = lookup.apply(task.processDefinitionId(), task.taskDefinitionKey());
    if (resolved == null || resolved.isBlank()) {
      resolved = task.name();
    }
    cache.put(key, resolved);
    return resolved;
  }
}
