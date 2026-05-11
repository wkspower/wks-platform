package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Story 5.1 AC1 / AC5 — unit tests for {@link FormValidator}. Mirrors the {@code
 * MappingValidatorTest} idiom: every "wrong axis" test asserts the error list contains the expected
 * wire code without asserting list size unless explicitly testing collect-all.
 *
 * <p>All tests are unit tests (no {@code @SpringBootTest}, no Postgres IT) per AC5.
 */
class FormValidatorTest {

  private final FormValidator validator = new FormValidator();

  // ---- null / empty forms — no-op ----

  @Test
  void nullFormsConfig_noErrors() {
    List<ErrorDetail> errors = new ArrayList<>();
    validator.validate(null, errors);
    assertThat(errors).isEmpty();
  }

  @Test
  void emptyDefinitionsList_noErrors() {
    List<ErrorDetail> errors = new ArrayList<>();
    validator.validate(RawFormConfig.fromList(List.of()), errors);
    assertThat(errors).isEmpty();
  }

  // ---- AC1 happy-path: valid triplets ----

  @Test
  void validTriplet_singleMonolithicSinglePage_noErrors() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def = new RawFormDefinition("intake", "single", "monolithic", "single-page", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);
    assertThat(errors).isEmpty();
  }

  @Test
  void validTriplet_singleSectionedMultiSection_noErrors() {
    List<ErrorDetail> errors = new ArrayList<>();
    // Story 5.3: sectioned forms require at least one section with id + label
    var section = new RawFormSection("personal", "Personal Information", List.of());
    var def =
        new RawFormDefinition(
            "onboarding", "single", "sectioned", "multi-section", null, List.of(section), null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);
    assertThat(errors).isEmpty();
  }

  // ---- AC1 WKS-FORM-001: parallel topology rejected ----

  @Test
  void parallelTopology_rejected_withWksForm001() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def =
        new RawFormDefinition("parallel-form", "parallel", "sectioned", "multi-section", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors).isNotEmpty();
    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-FORM-001");
              assertThat(e.message()).contains("Phase-1 capability");
              assertThat(e.field()).contains("topology");
            });
  }

  @Test
  void parallelTopology_message_containsUseTopologySingle() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def = new RawFormDefinition("x", "parallel", "monolithic", "single-page", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors).anySatisfy(e -> assertThat(e.message()).contains("use topology: single"));
  }

  // ---- AC1 invalid axis values ----

  @Test
  void unknownTopology_rejected_withWksForm001() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def = new RawFormDefinition("x", "distributed", "monolithic", "single-page", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-FORM-001");
              assertThat(e.field()).contains("topology");
            });
  }

  @Test
  void invalidDataModel_rejected() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def = new RawFormDefinition("x", "single", "flat", "single-page", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-CFG-008");
              assertThat(e.message()).contains("flat");
              assertThat(e.field()).contains("dataModel");
            });
  }

  @Test
  void invalidRendering_rejected() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def = new RawFormDefinition("x", "single", "monolithic", "full-page", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-CFG-008");
              assertThat(e.message()).contains("full-page");
              assertThat(e.field()).contains("rendering");
            });
  }

  // ---- AC1 missing axis values ----

  @Test
  void missingTopology_reportsWksCfg001() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def = new RawFormDefinition("x", null, "monolithic", "single-page", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-CFG-001");
              assertThat(e.field()).contains("topology");
            });
  }

  @Test
  void missingDataModel_reportsWksCfg001() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def = new RawFormDefinition("x", "single", null, "single-page", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-CFG-001");
              assertThat(e.field()).contains("dataModel");
            });
  }

  @Test
  void missingRendering_reportsWksCfg001() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def = new RawFormDefinition("x", "single", "monolithic", null, null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-CFG-001");
              assertThat(e.field()).contains("rendering");
            });
  }

  // ---- collect-all: multiple axis errors in one definition ----

  @Test
  void allAxesMissing_collectsThreeErrors() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def = new RawFormDefinition("x", null, null, null, null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors).hasSize(3);
    assertThat(errors).extracting(ErrorDetail::code).containsOnly("WKS-CFG-001");
  }

  // ---- multiple definitions in one RawFormConfig ----

  @Test
  void multipleDefinitions_validThenInvalid_errorsOnlyForInvalid() {
    List<ErrorDetail> errors = new ArrayList<>();
    var valid = new RawFormDefinition("intake", "single", "monolithic", "single-page", null, null, null);
    var invalid = new RawFormDefinition("bad", "parallel", "sectioned", "multi-section", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(valid, invalid)), errors);

    // Only the second definition should produce errors
    assertThat(errors).isNotEmpty();
    assertThat(errors).allSatisfy(e -> assertThat(e.field()).contains("/forms/1"));
  }

  // ---- Story 5.3: section validation for dataModel: sectioned ----

  @Test
  void sectioned_withoutSections_emitsWksCfg001() {
    List<ErrorDetail> errors = new ArrayList<>();
    // No sections provided — should require at least one
    var def =
        new RawFormDefinition("bank-form", "single", "sectioned", "multi-section", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-CFG-001");
              assertThat(e.field()).contains("/sections");
              assertThat(e.message()).contains("sectioned");
            });
  }

  @Test
  void sectioned_withEmptySectionsList_emitsWksCfg001() {
    List<ErrorDetail> errors = new ArrayList<>();
    var def =
        new RawFormDefinition("bank-form", "single", "sectioned", "multi-section", null, List.of(), null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-CFG-001");
              assertThat(e.field()).contains("/sections");
            });
  }

  @Test
  void sectioned_withValidSections_passes() {
    List<ErrorDetail> errors = new ArrayList<>();
    var section = new RawFormSection("personal", "Personal Information", List.of());
    var def =
        new RawFormDefinition(
            "bank-form", "single", "sectioned", "multi-section", null, List.of(section), null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors).isEmpty();
  }

  @Test
  void sectioned_sectionMissingLabel_emitsWksCfg001() {
    List<ErrorDetail> errors = new ArrayList<>();
    var section = new RawFormSection("personal", null, List.of());
    var def =
        new RawFormDefinition(
            "bank-form", "single", "sectioned", "multi-section", null, List.of(section), null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-CFG-001");
              assertThat(e.field()).contains("/sections/0/label");
            });
  }

  @Test
  void sectioned_sectionMissingId_emitsWksCfg001() {
    List<ErrorDetail> errors = new ArrayList<>();
    var section = new RawFormSection(null, "Personal Information", List.of());
    var def =
        new RawFormDefinition(
            "bank-form", "single", "sectioned", "multi-section", null, List.of(section), null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors)
        .anySatisfy(
            e -> {
              assertThat(e.code()).isEqualTo("WKS-CFG-001");
              assertThat(e.field()).contains("/sections/0/id");
            });
  }

  @Test
  void monolithic_withoutSections_noSectionErrors() {
    List<ErrorDetail> errors = new ArrayList<>();
    // monolithic forms do not need sections — no section errors expected
    var def = new RawFormDefinition("intake", "single", "monolithic", "single-page", null, null, null);
    validator.validate(RawFormConfig.fromList(List.of(def)), errors);

    assertThat(errors).isEmpty();
  }
}
