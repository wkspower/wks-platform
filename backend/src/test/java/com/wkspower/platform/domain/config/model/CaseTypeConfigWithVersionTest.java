package com.wkspower.platform.domain.config.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CaseTypeConfig#withVersion(int)} (Story 3.4 / Decision 20). Verifies the
 * record-copy semantics the version-registry override depends on.
 */
class CaseTypeConfigWithVersionTest {

  private static CaseTypeConfig sample(int version) {
    return new CaseTypeConfig(
        "loan",
        "Loan",
        version,
        "desc",
        new WorkflowRef("loan.bpmn"),
        List.of(
            new FieldDefinition("name", "Name", FieldType.TEXT, true, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("admin", List.of(Permission.VIEW))),
        List.of(),
        List.of());
  }

  @Test
  void identityCopyWhenSameVersion() {
    CaseTypeConfig original = sample(3);
    CaseTypeConfig copy = original.withVersion(3);
    assertThat(copy.version()).isEqualTo(3);
    assertThat(copy).isEqualTo(original);
  }

  @Test
  void distinctCopyWhenNewVersion() {
    CaseTypeConfig original = sample(1);
    CaseTypeConfig bumped = original.withVersion(7);
    assertThat(bumped.version()).isEqualTo(7);
    assertThat(bumped.id()).isEqualTo(original.id());
    assertThat(bumped.displayName()).isEqualTo(original.displayName());
    assertThat(bumped.workflow()).isEqualTo(original.workflow());
    assertThat(bumped.fields()).isEqualTo(original.fields());
    assertThat(bumped.statuses()).isEqualTo(original.statuses());
    assertThat(bumped.listColumns()).isEqualTo(original.listColumns());
    assertThat(bumped.roles()).isEqualTo(original.roles());
    assertThat(bumped.stages()).isEqualTo(original.stages());
    assertThat(bumped).isNotEqualTo(original);
  }
}
