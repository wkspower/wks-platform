package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.FormSection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Story 5.3 — unit tests for {@link FormDefinitionMapper} section mapping. Verifies that sections
 * round-trip from raw YAML structures to domain {@link FormSection} records with fields intact.
 */
class FormDefinitionMapperTest {

  // ---- monolithic forms (no sections) ----

  @Test
  void monolithic_noSections_sectionsEmptyList() {
    var raw =
        new RawFormDefinition(
            "intake",
            "single",
            "monolithic",
            "single-page",
            List.of(
                Map.of(
                    "id",
                    "name",
                    "displayName",
                    "Full Name",
                    "type",
                    "text",
                    "required",
                    true,
                    "order",
                    0)),
            null,
            null);
    FormDefinition result = FormDefinitionMapper.toDomain(raw);

    assertThat(result.sections()).isEmpty();
    assertThat(result.fields()).hasSize(1);
    assertThat(result.fields().get(0).id()).isEqualTo("name");
  }

  // ---- sectioned forms ----

  @Test
  void sectioned_twoSections_fieldsIntact() {
    var personalFields =
        List.of(
            Map.<String, Object>of(
                "id",
                "firstName",
                "displayName",
                "First Name",
                "type",
                "text",
                "required",
                true,
                "order",
                1),
            Map.<String, Object>of(
                "id",
                "lastName",
                "displayName",
                "Last Name",
                "type",
                "text",
                "required",
                true,
                "order",
                2));
    var employmentFields =
        List.of(
            Map.<String, Object>of(
                "id",
                "employer",
                "displayName",
                "Employer",
                "type",
                "text",
                "required",
                false,
                "order",
                1));

    var sections =
        List.of(
            new RawFormSection("personal", "Personal Information", personalFields),
            new RawFormSection("employment", "Employment Details", employmentFields));

    var raw =
        new RawFormDefinition("bank-form", "single", "sectioned", "multi-section", null, sections, null);
    FormDefinition result = FormDefinitionMapper.toDomain(raw);

    assertThat(result.sections()).hasSize(2);

    FormSection personal = result.sections().get(0);
    assertThat(personal.id()).isEqualTo("personal");
    assertThat(personal.label()).isEqualTo("Personal Information");
    assertThat(personal.fields()).hasSize(2);
    assertThat(personal.fields())
        .extracting(f -> f.id())
        .containsExactlyInAnyOrder("firstName", "lastName");

    FormSection employment = result.sections().get(1);
    assertThat(employment.id()).isEqualTo("employment");
    assertThat(employment.label()).isEqualTo("Employment Details");
    assertThat(employment.fields()).hasSize(1);
    assertThat(employment.fields().get(0).id()).isEqualTo("employer");
  }

  @Test
  void sectioned_sectionWithoutLabel_defaultsToId() {
    var section = new RawFormSection("personal", null, List.of());
    var raw =
        new RawFormDefinition(
            "bank-form", "single", "sectioned", "multi-section", null, List.of(section), null);
    FormDefinition result = FormDefinitionMapper.toDomain(raw);

    // label defaults to id when absent
    assertThat(result.sections()).hasSize(1);
    assertThat(result.sections().get(0).label()).isEqualTo("personal");
  }

  @Test
  void sectioned_sectionWithBlankId_skipped() {
    var blankId = new RawFormSection("", "Section A", List.of());
    var valid = new RawFormSection("employment", "Employment", List.of());
    var raw =
        new RawFormDefinition(
            "bank-form", "single", "sectioned", "multi-section", null, List.of(blankId, valid), null);
    FormDefinition result = FormDefinitionMapper.toDomain(raw);

    // blank-id section is skipped; valid section remains
    assertThat(result.sections()).hasSize(1);
    assertThat(result.sections().get(0).id()).isEqualTo("employment");
  }

  @Test
  void sectioned_nullSectionEntry_skipped() {
    var sections = new java.util.ArrayList<RawFormSection>();
    sections.add(null);
    sections.add(new RawFormSection("employment", "Employment", List.of()));
    var raw =
        new RawFormDefinition("bank-form", "single", "sectioned", "multi-section", null, sections, null);
    FormDefinition result = FormDefinitionMapper.toDomain(raw);

    assertThat(result.sections()).hasSize(1);
    assertThat(result.sections().get(0).id()).isEqualTo("employment");
  }

  // ---- Story 6.1 — archetype round-trip ----

  @Test
  void archetype_presentInRaw_roundTripsToDomainsRecord() {
    var raw =
        new RawFormDefinition(
            "final-form",
            "single",
            "monolithic",
            "single-page",
            List.of(
                Map.of(
                    "id",
                    "note",
                    "displayName",
                    "Note",
                    "type",
                    "text",
                    "required",
                    false,
                    "order",
                    0)),
            null,
            "business_final");
    FormDefinition result = FormDefinitionMapper.toDomain(raw);
    assertThat(result.archetype()).isEqualTo("business_final");
  }

  @Test
  void archetype_null_nullInDomainRecord() {
    var raw =
        new RawFormDefinition(
            "draft-form",
            "single",
            "monolithic",
            "single-page",
            List.of(
                Map.of(
                    "id",
                    "note",
                    "displayName",
                    "Note",
                    "type",
                    "text",
                    "required",
                    false,
                    "order",
                    0)),
            null,
            null);
    FormDefinition result = FormDefinitionMapper.toDomain(raw);
    assertThat(result.archetype()).isNull();
  }
}
