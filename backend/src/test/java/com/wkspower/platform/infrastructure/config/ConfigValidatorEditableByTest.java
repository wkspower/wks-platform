package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.ValidationResult;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Story 5.6 AC1 — validator coverage for {@code editableBy: [role:&lt;id&gt;]} on field
 * declarations. Surfaces malformed entries and unknown role references as {@code WKS-FORM-001}.
 */
class ConfigValidatorEditableByTest {

  private final CaseTypeYamlLoader loader = new CaseTypeYamlLoader();
  private final ConfigValidator validator = new ConfigValidator();

  @Test
  void validRoleScopedEditableByPasses() {
    ValidationResult result = validate(yamlWithEditableBy("[role:officer]"));

    assertThat(result.isInvalid())
        .as("valid editableBy must not error: %s", result.errors())
        .isFalse();
    var f =
        result.config().get().fields().stream()
            .filter(x -> x.id().equals("amount"))
            .findFirst()
            .get();
    assertThat(f.editableBy()).containsExactly("role:officer");
  }

  @Test
  void malformedEditableByEntryEmitsWksForm001() {
    // "underwriter" without role: prefix
    ValidationResult result = validate(yamlWithEditableBy("[\"underwriter\"]"));

    var match =
        result.errors().stream()
            .filter(
                e ->
                    "WKS-FORM-001".equals(e.code())
                        && e.field() != null
                        && e.field().contains("editableBy"))
            .findFirst();
    assertThat(match)
        .as("malformed editableBy must emit WKS-FORM-001 — got: %s", result.errors())
        .isPresent();
    assertThat(match.get().message()).contains("role:");
  }

  @Test
  void unknownRoleIdEmitsWksForm001() {
    ValidationResult result = validate(yamlWithEditableBy("[role:supervisor]"));

    var match =
        result.errors().stream()
            .filter(
                e ->
                    "WKS-FORM-001".equals(e.code())
                        && e.field() != null
                        && e.field().contains("editableBy"))
            .findFirst();
    assertThat(match)
        .as("unknown role id must emit WKS-FORM-001 — got: %s", result.errors())
        .isPresent();
    assertThat(match.get().message()).contains("supervisor");
  }

  @Test
  void emptyEditableByListIsTreatedAsOmitted() {
    ValidationResult result = validate(yamlWithEditableBy("[]"));

    assertThat(result.isInvalid()).as("empty list is legal — no error").isFalse();
    var f =
        result.config().get().fields().stream()
            .filter(x -> x.id().equals("amount"))
            .findFirst()
            .get();
    assertThat(f.editableBy()).isEmpty();
  }

  // ---- helpers ---------------------------------------------------------------

  private ValidationResult validate(String yaml) {
    var r = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    if (!r.isParsed()) {
      return ValidationResult.invalid(r.errors());
    }
    return validator.validate(r.raw(), r.lines());
  }

  /**
   * Build a case-type YAML with the supplied {@code editableBy} flow-list expression attached to
   * the {@code amount} field.
   */
  private static String yamlWithEditableBy(String editableByExpr) {
    return ""
        + "id: ct-editable-by\n"
        + "displayName: Editable Test\n"
        + "version: 1\n"
        + "fields:\n"
        + "  - id: applicant\n"
        + "    displayName: Applicant\n"
        + "    type: text\n"
        + "    required: true\n"
        + "  - id: amount\n"
        + "    displayName: Amount\n"
        + "    type: number\n"
        + "    editableBy: "
        + editableByExpr
        + "\n"
        + "statuses:\n"
        + "  - id: open\n"
        + "    displayName: Open\n"
        + "    color: amber\n"
        + "listColumns: [applicant]\n"
        + "roles:\n"
        + "  - name: officer\n"
        + "    permissions: [view, edit]\n";
  }
}
