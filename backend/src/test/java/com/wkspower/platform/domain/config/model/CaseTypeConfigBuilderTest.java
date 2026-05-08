package com.wkspower.platform.domain.config.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Story 5.4 AC-0 — verifies {@link CaseTypeConfig.Builder} produces a record equivalent to the
 * canonical 11-arg constructor across full and minimal field permutations.
 */
class CaseTypeConfigBuilderTest {

  @Test
  void builderProducesSameRecordAsCanonicalConstructor() {
    WorkflowRef wf = new WorkflowRef("loan.bpmn");
    List<FieldDefinition> fields =
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null));
    List<StatusDefinition> statuses =
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC));
    List<String> listColumns = List.of("name");
    List<RoleDefinition> roles = List.of(new RoleDefinition("admin", List.of(Permission.VIEW)));

    CaseTypeConfig fromCanonical =
        new CaseTypeConfig(
            "loan",
            "Loan",
            3,
            "desc",
            wf,
            fields,
            statuses,
            listColumns,
            roles,
            List.of(),
            List.of());

    CaseTypeConfig fromBuilder =
        CaseTypeConfig.builder()
            .id("loan")
            .displayName("Loan")
            .version(3)
            .description("desc")
            .workflow(wf)
            .fields(fields)
            .statuses(statuses)
            .listColumns(listColumns)
            .roles(roles)
            .stages(List.of())
            .forms(List.of())
            .build();

    assertThat(fromBuilder).isEqualTo(fromCanonical);
  }

  @Test
  void builderDefaultsCollectionsToEmpty() {
    CaseTypeConfig built = CaseTypeConfig.builder().id("x").displayName("X").version(1).build();

    assertThat(built.fields()).isEmpty();
    assertThat(built.statuses()).isEmpty();
    assertThat(built.listColumns()).isEmpty();
    assertThat(built.roles()).isEmpty();
    assertThat(built.stages()).isEmpty();
    assertThat(built.forms()).isEmpty();
    assertThat(built.workflow()).isNull();
    assertThat(built.description()).isNull();
  }
}
