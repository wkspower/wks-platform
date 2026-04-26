package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.event.ConfigDeployed;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Story 2.5 AC11 #3 — locks in the {@code (caseTypeId, version)} schema cache contract on {@link
 * CaseDataValidatorAdapter}. Two invariants:
 *
 * <ul>
 *   <li>Repeat validations against the same {@code (id, version)} pair compile the schema once.
 *   <li>A {@link ConfigDeployed} event for an id evicts every cached entry under that id so a
 *       redeployed schema applies on the next validate call.
 * </ul>
 */
class CaseDataValidatorAdapterTest {

  private final JsonSchemaGenerator generator = spy(new JsonSchemaGenerator());
  private final ObjectMapper mapper = new ObjectMapper();
  private final CaseDataValidatorAdapter adapter = new CaseDataValidatorAdapter(generator, mapper);

  @Test
  void cachesSchemaPerVersion() {
    CaseTypeConfig v1 = caseType("loan-application", 1);
    Map<String, Object> data = Map.of("name", "Asha");

    adapter.validate(v1, data);
    adapter.validate(v1, data);
    adapter.validate(v1, data);

    // Schema generation is the expensive build step we're caching.
    verify(generator, times(1)).generate(v1);
    assertThat(adapter.cacheSize()).isEqualTo(1);
  }

  @Test
  void differentVersionsGetDistinctCacheEntries() {
    CaseTypeConfig v1 = caseType("loan-application", 1);
    CaseTypeConfig v2 = caseType("loan-application", 2);

    adapter.validate(v1, Map.of("name", "Asha"));
    adapter.validate(v2, Map.of("name", "Bharat"));
    adapter.validate(v1, Map.of("name", "Asha"));

    verify(generator, times(1)).generate(v1);
    verify(generator, times(1)).generate(v2);
    assertThat(adapter.cacheSize()).isEqualTo(2);
  }

  @Test
  void invalidatesOnConfigDeployed() {
    CaseTypeConfig v1 = caseType("loan-application", 1);
    CaseTypeConfig v2 = caseType("loan-application", 2);
    CaseTypeConfig other = caseType("hr-onboarding", 1);

    adapter.validate(v1, Map.of("name", "Asha"));
    adapter.validate(v2, Map.of("name", "Bharat"));
    adapter.validate(other, Map.of("name", "Chandra"));
    assertThat(adapter.cacheSize()).isEqualTo(3);

    adapter.onConfigDeployed(
        new ConfigDeployed(
            "loan-application",
            2,
            "deployment-1",
            "loan-application",
            "loan-application:1:1",
            null,
            Instant.now()));

    // Both loan-application entries (v1 + v2) cleared; hr-onboarding survives.
    assertThat(adapter.cacheSize()).isEqualTo(1);

    // Re-validation rebuilds the loan schema once; hr-onboarding cache hit (no extra build).
    adapter.validate(v1, Map.of("name", "Asha"));
    verify(generator, atLeast(2)).generate(v1);
    verify(generator, times(1)).generate(other);
  }

  @Test
  void invalidationForUnrelatedCaseTypeIsNoop() {
    CaseTypeConfig loan = caseType("loan-application", 1);
    adapter.validate(loan, Map.of("name", "Asha"));
    assertThat(adapter.cacheSize()).isEqualTo(1);

    adapter.onConfigDeployed(
        new ConfigDeployed("hr-onboarding", 1, "d", "hr", "hr:1:1", null, Instant.now()));

    assertThat(adapter.cacheSize()).isEqualTo(1);
    adapter.validate(loan, Map.of("name", "Asha"));
    verify(generator, atMost(1)).generate(loan);
  }

  private static CaseTypeConfig caseType(String id, int version) {
    return new CaseTypeConfig(
        id,
        id,
        version,
        null,
        new WorkflowRef(id + ".bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("admin", List.of(Permission.VIEW, Permission.CREATE))));
  }
}
