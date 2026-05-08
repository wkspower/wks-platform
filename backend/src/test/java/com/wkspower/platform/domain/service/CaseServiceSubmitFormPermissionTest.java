package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.DefaultFieldEditability;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Story 5.6 AC2 — unit coverage for the static {@link CaseService#checkFieldEditPermissions} helper
 * that drives WKS-AUTHZ-001 rejection inside {@code submitForm}. Pure-function tests; no Spring, no
 * DB, no engine.
 */
class CaseServiceSubmitFormPermissionTest {

  @Test
  void noEditableByWithDefaultEditableAllowsAllRoles() {
    CaseTypeConfig ct = caseType(DefaultFieldEditability.EDITABLE_BY_DEFAULT, List.of());

    List<ErrorDetail> v =
        CaseService.checkFieldEditPermissions(ct, Set.of("amount"), Set.of(/*no roles*/ ));

    assertThat(v).isEmpty();
  }

  @Test
  void noEditableByWithLockedByDefaultRejectsAllRoles() {
    CaseTypeConfig ct = caseType(DefaultFieldEditability.LOCKED_BY_DEFAULT, List.of());

    List<ErrorDetail> v =
        CaseService.checkFieldEditPermissions(ct, Set.of("amount"), Set.of("admin"));

    assertThat(v).hasSize(1);
    assertThat(v.get(0).code()).isEqualTo(ErrorCode.WKS_AUTHZ_FIELD.wire());
    assertThat(v.get(0).field()).isEqualTo("fields.amount");
    assertThat(v.get(0).message()).contains("locked-by-default");
  }

  @Test
  void editableByMatchingRoleAllowed() {
    CaseTypeConfig ct =
        caseType(DefaultFieldEditability.EDITABLE_BY_DEFAULT, List.of("role:underwriter"));

    List<ErrorDetail> v =
        CaseService.checkFieldEditPermissions(ct, Set.of("amount"), Set.of("underwriter"));

    assertThat(v).isEmpty();
  }

  @Test
  void editableByWithoutMatchingRoleRejected() {
    CaseTypeConfig ct =
        caseType(DefaultFieldEditability.EDITABLE_BY_DEFAULT, List.of("role:underwriter"));

    List<ErrorDetail> v =
        CaseService.checkFieldEditPermissions(ct, Set.of("amount"), Set.of("officer"));

    assertThat(v).hasSize(1);
    ErrorDetail e = v.get(0);
    assertThat(e.code()).isEqualTo(ErrorCode.WKS_AUTHZ_FIELD.wire());
    assertThat(e.field()).isEqualTo("fields.amount");
    assertThat(e.message()).contains("underwriter");
    assertThat(e.message()).contains("actor lacks");
  }

  @Test
  void multipleChangedFieldsBothRejectedSurfaceTwoViolations() {
    // Two fields, each editableBy underwriter; actor has no roles → both rejected.
    CaseTypeConfig ct =
        new CaseTypeConfig(
            "ct",
            "CT",
            1,
            null,
            null,
            List.of(
                fieldEditableBy("amount", "Amount", FieldType.NUMBER, List.of("role:underwriter")),
                fieldEditableBy(
                    "approver", "Approver", FieldType.TEXT, List.of("role:underwriter"))),
            List.of(
                new com.wkspower.platform.domain.config.model.StatusDefinition(
                    "open", "Open", com.wkspower.platform.domain.config.model.StatusColor.ZINC)),
            List.of("amount"),
            List.of(new RoleDefinition("underwriter", List.of(Permission.EDIT))),
            List.of(),
            List.of(),
            DefaultFieldEditability.EDITABLE_BY_DEFAULT);

    List<ErrorDetail> v =
        CaseService.checkFieldEditPermissions(ct, Set.of("amount", "approver"), Set.of());

    assertThat(v).hasSize(2);
    assertThat(v).allSatisfy(e -> assertThat(e.code()).isEqualTo(ErrorCode.WKS_AUTHZ_FIELD.wire()));
    assertThat(v)
        .extracting(ErrorDetail::field)
        .containsExactlyInAnyOrder("fields.amount", "fields.approver");
  }

  @Test
  void unchangedFieldNotChecked() {
    // No fields in the diff — no permission check runs even if the field has restrictive
    // editableBy.
    CaseTypeConfig ct =
        caseType(DefaultFieldEditability.EDITABLE_BY_DEFAULT, List.of("role:underwriter"));

    List<ErrorDetail> v =
        CaseService.checkFieldEditPermissions(ct, Set.of(), Set.of(/*no roles*/ ));

    assertThat(v).isEmpty();
  }

  @Test
  void unknownFieldIdSkippedByPermissionCheck() {
    // Unknown field ids in the diff (e.g. typo / orphan column) are silently skipped — WKS-FORM-002
    // owns unknown-field rejection on a different code path.
    CaseTypeConfig ct = caseType(DefaultFieldEditability.LOCKED_BY_DEFAULT, List.of());

    List<ErrorDetail> v =
        CaseService.checkFieldEditPermissions(ct, Set.of("unknown-field"), Set.of("admin"));

    assertThat(v).isEmpty();
  }

  // ---- helpers ---------------------------------------------------------------

  private static CaseTypeConfig caseType(
      DefaultFieldEditability defaultEditability, List<String> editableBy) {
    return new CaseTypeConfig(
        "ct",
        "CT",
        1,
        null,
        null,
        List.of(fieldEditableBy("amount", "Amount", FieldType.NUMBER, editableBy)),
        List.of(
            new com.wkspower.platform.domain.config.model.StatusDefinition(
                "open", "Open", com.wkspower.platform.domain.config.model.StatusColor.ZINC)),
        List.of("amount"),
        List.of(new RoleDefinition("underwriter", List.of(Permission.EDIT))),
        List.of(),
        List.of(),
        defaultEditability);
  }

  private static FieldDefinition fieldEditableBy(
      String id, String dn, FieldType type, List<String> editableBy) {
    return new FieldDefinition(id, dn, type, false, false, 0, List.of(), null, editableBy);
  }
}
