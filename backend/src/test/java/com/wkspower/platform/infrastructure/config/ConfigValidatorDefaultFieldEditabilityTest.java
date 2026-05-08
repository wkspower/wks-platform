package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.DefaultFieldEditability;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Story 5.6 AC4 — validator coverage for the top-level {@code defaultFieldEditability} key. Default
 * on omit: {@link DefaultFieldEditability#EDITABLE_BY_DEFAULT} (preserves pre-5.6 behavior).
 */
class ConfigValidatorDefaultFieldEditabilityTest {

  private final CaseTypeYamlLoader loader = new CaseTypeYamlLoader();
  private final ConfigValidator validator = new ConfigValidator();

  @Test
  void omittedKeyDefaultsToEditableByDefault() {
    ValidationResult result = validate(yamlWith(""));
    assertThat(result.isInvalid()).isFalse();
    assertThat(result.config().get().defaultFieldEditability())
        .isEqualTo(DefaultFieldEditability.EDITABLE_BY_DEFAULT);
  }

  @Test
  void validLockedByDefaultParsed() {
    ValidationResult result = validate(yamlWith("defaultFieldEditability: locked-by-default\n"));
    assertThat(result.isInvalid()).isFalse();
    assertThat(result.config().get().defaultFieldEditability())
        .isEqualTo(DefaultFieldEditability.LOCKED_BY_DEFAULT);
  }

  @Test
  void unknownLiteralEmitsWksForm001() {
    ValidationResult result = validate(yamlWith("defaultFieldEditability: read-only\n"));
    var match =
        result.errors().stream()
            .filter(
                e -> "WKS-FORM-001".equals(e.code()) && "defaultFieldEditability".equals(e.field()))
            .findFirst();
    assertThat(match).as("got: %s", result.errors()).isPresent();
    assertThat(match.get().message()).contains("read-only");
  }

  // ---- helpers ---------------------------------------------------------------

  private ValidationResult validate(String yaml) {
    var r = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    if (!r.isParsed()) {
      return ValidationResult.invalid(r.errors());
    }
    return validator.validate(r.raw(), r.lines());
  }

  private static String yamlWith(String defaultEditabilityLine) {
    return ""
        + "id: ct-dfe\n"
        + "displayName: Default Editability Test\n"
        + "version: 1\n"
        + defaultEditabilityLine
        + "fields:\n"
        + "  - id: applicant\n"
        + "    displayName: Applicant\n"
        + "    type: text\n"
        + "    required: true\n"
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
