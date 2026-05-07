package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Story 5.1 AC2 / AC5 — confirms that {@link ConfigValidator} delegates to {@link FormValidator}
 * and merges its errors into the shared {@link ValidationResult}. No {@code @SpringBootTest}; no
 * Postgres IT required (AC5).
 *
 * <p>The test exercises the {@code ConfigValidator(MappingValidator, FormValidator)} constructor
 * path — same injection pattern as {@link ConfigValidatorMappingIntegrationTest} uses for {@link
 * MappingValidator}.
 */
class ConfigValidatorFormDelegationTest {

  private final FormValidator formValidator = new FormValidator();
  private final ConfigValidator validator = new ConfigValidator(null, formValidator);
  private final CaseTypeYamlLoader loader = new CaseTypeYamlLoader();

  // ---- AC2: errors from FormValidator are merged into ConfigValidator result ----

  @Test
  void parallelTopologyInFormsBlock_surfacesWksForm001InValidationResult() {
    var yaml =
        """
        id: loan-app
        displayName: Loan Application
        version: 1
        roles:
          - name: officer
            permissions: [view]
        forms:
          - id: intake-form
            topology: parallel
            dataModel: sectioned
            rendering: multi-section
        """;
    var result = validate(yaml);

    assertThat(result.isInvalid()).isTrue();
    assertErrorContains(result.errors(), "WKS-FORM-001");
  }

  @Test
  void invalidDataModelInFormsBlock_surfacesWksCfg008() {
    var yaml =
        """
        id: loan-app
        displayName: Loan Application
        version: 1
        roles:
          - name: officer
            permissions: [view]
        forms:
          - id: intake-form
            topology: single
            dataModel: flat
            rendering: single-page
        """;
    var result = validate(yaml);

    assertThat(result.isInvalid()).isTrue();
    assertErrorContains(result.errors(), "WKS-CFG-008");
  }

  @Test
  void validFormsBlock_doesNotAddErrors() {
    var yaml =
        """
        id: loan-app
        displayName: Loan Application
        version: 1
        roles:
          - name: officer
            permissions: [view]
        forms:
          - id: intake-form
            topology: single
            dataModel: monolithic
            rendering: single-page
        """;
    var result = validate(yaml);

    assertThat(result.isInvalid()).as("valid forms block should not produce errors").isFalse();
    // No form-related errors
    assertThat(result.errors())
        .noneMatch(
            e ->
                e.code().startsWith("WKS-FORM")
                    || (e.field() != null && e.field().contains("/forms/")));
  }

  @Test
  void caseTypeWithoutFormsBlock_backwardCompat_noErrors() {
    var yaml =
        """
        id: loan-app
        displayName: Loan Application
        version: 1
        roles:
          - name: officer
            permissions: [view]
        """;
    var result = validate(yaml);

    assertThat(result.isInvalid()).as("CaseType without forms: should load cleanly").isFalse();
  }

  @Test
  void formErrors_mergedAlongsideCaseTypeErrors_collectAll() {
    // CaseType has a bad version (WKS-CFG-002) AND a bad form topology (WKS-FORM-001)
    // Both errors must appear in the collected result — collect-all invariant.
    var yaml =
        """
        id: loan-app
        displayName: Loan Application
        version: -1
        roles:
          - name: officer
            permissions: [view]
        forms:
          - id: intake-form
            topology: parallel
            dataModel: sectioned
            rendering: multi-section
        """;
    var result = validate(yaml);

    assertThat(result.isInvalid()).isTrue();
    assertErrorContains(result.errors(), "WKS-CFG-002");
    assertErrorContains(result.errors(), "WKS-FORM-001");
  }

  // ---- AC3: ConfigValidator is null-safe when FormValidator not wired ----

  @Test
  void configValidatorWithoutFormValidator_validFormsBlock_noNpe() {
    // ConfigValidator with no FormValidator (legacy single-arg constructor path) should not NPE
    // when forms: block is present in YAML — the null check in validate() guards this.
    var validatorNoForms = new ConfigValidator((MappingValidator) null);
    var yaml =
        """
        id: loan-app
        displayName: Loan Application
        version: 1
        roles:
          - name: officer
            permissions: [view]
        forms:
          - id: intake-form
            topology: single
            dataModel: monolithic
            rendering: single-page
        """;
    var result = validateWith(validatorNoForms, yaml);
    // No NPE — the validator simply skips form validation when formValidator is null
    assertThat(result).isNotNull();
    assertThat(result.isInvalid()).isFalse();
  }

  // ---- helpers ----

  private ValidationResult validate(String yaml) {
    return validateWith(validator, yaml);
  }

  private ValidationResult validateWith(ConfigValidator cv, String yaml) {
    var r = loader.readBytes("test.yaml", yaml.getBytes(StandardCharsets.UTF_8));
    if (!r.isParsed()) {
      return ValidationResult.invalid(r.errors());
    }
    return cv.validate(r.raw(), r.lines());
  }

  private static void assertErrorContains(List<ErrorDetail> errors, String code) {
    assertThat(errors)
        .as("expected error with code " + code)
        .anySatisfy(e -> assertThat(e.code()).isEqualTo(code));
  }
}
