package com.wkspower.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.port.CaseTypeReader;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CaseTypePermissionEvaluatorTest {

  private static final AuthenticatedUser OFFICER =
      new AuthenticatedUser(UUID.randomUUID(), "officer@x", Set.of("officer"));
  private static final AuthenticatedUser CUSTOMER =
      new AuthenticatedUser(UUID.randomUUID(), "customer@x", Set.of("customer"));

  @Test
  void hasVerbReturnsTrueWhenRoleHoldsTheVerb() {
    CaseTypePermissionEvaluator eval = new CaseTypePermissionEvaluator(reader(loanType()));

    assertThat(eval.hasVerb(OFFICER, "loan-application", "create")).isTrue();
    assertThat(eval.hasVerb(OFFICER, "loan-application", "view")).isTrue();
  }

  @Test
  void hasVerbReturnsFalseWhenUserRoleLacksTheVerb() {
    CaseTypePermissionEvaluator eval = new CaseTypePermissionEvaluator(reader(loanType()));

    assertThat(eval.hasVerb(CUSTOMER, "loan-application", "create")).isFalse();
  }

  @Test
  void hasVerbReturnsFalseWhenUserHasNoMatchingRole() {
    CaseTypePermissionEvaluator eval = new CaseTypePermissionEvaluator(reader(loanType()));
    AuthenticatedUser stranger = new AuthenticatedUser(UUID.randomUUID(), "x@x", Set.of("auditor"));

    assertThat(eval.hasVerb(stranger, "loan-application", "view")).isFalse();
  }

  @Test
  void hasVerbReturnsFalseWhenCaseTypeIsUnknown() {
    // Per code-review P4: throwing from inside @PreAuthorize SpEL gets wrapped to 500/403 by
    // Spring Security 6 — instead, return false here and let the service path produce the 404.
    CaseTypePermissionEvaluator eval = new CaseTypePermissionEvaluator(reader(loanType()));

    assertThat(eval.hasVerb(OFFICER, "no-such-type", "view")).isFalse();
  }

  @Test
  void nullInputsReturnFalse() {
    CaseTypePermissionEvaluator eval = new CaseTypePermissionEvaluator(reader(loanType()));

    assertThat(eval.hasVerb(null, "loan-application", "view")).isFalse();
    assertThat(eval.hasVerb(OFFICER, null, "view")).isFalse();
    assertThat(eval.hasVerb(OFFICER, "loan-application", null)).isFalse();
  }

  private static CaseTypeConfig loanType() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(
            new RoleDefinition("officer", List.of(Permission.VIEW, Permission.CREATE)),
            new RoleDefinition("customer", List.of(Permission.VIEW))));
  }

  private static CaseTypeReader reader(CaseTypeConfig config) {
    return new CaseTypeReader() {
      @Override
      public Optional<CaseTypeConfig> find(String id) {
        return id.equals(config.id()) ? Optional.of(config) : Optional.empty();
      }

      @Override
      public Collection<CaseTypeConfig> all() {
        return List.of(config);
      }
    };
  }
}
