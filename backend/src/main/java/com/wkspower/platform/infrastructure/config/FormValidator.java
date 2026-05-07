package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Story 5.1 — collect-all validator for the {@code forms: [...]} block of a CaseType YAML.
 *
 * <p>Mirrors the {@link MappingValidator} idiom: every check accumulates {@link ErrorDetail}s into
 * the shared error list and the validator never short-circuits on first error (architecture
 * invariant — collect-all design).
 *
 * <p>This validator is I/O-free and schema-only. It enforces the <b>three-axis vocabulary</b>:
 *
 * <ul>
 *   <li>{@code topology} — Phase-0 allows only {@code single}; {@code parallel} is Phase-1 and
 *       produces {@link ErrorCode#WKS_FORM_001}.
 *   <li>{@code dataModel} — allowed values: {@code monolithic | sectioned}.
 *   <li>{@code rendering} — allowed values: {@code single-page | multi-section}.
 * </ul>
 *
 * <h2>Wire codes emitted</h2>
 *
 * <ul>
 *   <li>{@code WKS-FORM-001} — {@code topology: parallel} (or any non-{@code single} value) is a
 *       Phase-1 capability rejected in Phase 0.
 * </ul>
 *
 * <p>Story 5.1 — schema-only; no runtime or persistence concern.
 */
@Component
public class FormValidator {

  /** Phase-0 topology allow-list. {@code parallel} is reserved for Phase 1. */
  private static final Set<String> ALLOWED_TOPOLOGIES = Set.of("single");

  /** Allowed {@code dataModel} values. */
  private static final Set<String> ALLOWED_DATA_MODELS = Set.of("monolithic", "sectioned");

  /** Allowed {@code rendering} values. */
  private static final Set<String> ALLOWED_RENDERINGS = Set.of("single-page", "multi-section");

  /**
   * Validate the {@code forms} block and append any errors to {@code errors}. The method mirrors
   * {@link MappingValidator#validate(RawCaseTypeConfig, java.util.Set, java.util.Map)} in pattern:
   * null or empty config is a no-op; every offence is appended without short-circuiting.
   *
   * @param forms deserialized forms config; {@code null} or empty-definitions means no forms
   *     declared — no error (backward-compat, AC3).
   * @param errors mutable list into which validation errors are appended (shared with caller).
   */
  public void validate(RawFormConfig forms, List<ErrorDetail> errors) {
    if (forms == null || forms.definitions().isEmpty()) {
      return;
    }

    List<RawFormDefinition> defs = forms.definitions();
    for (int i = 0; i < defs.size(); i++) {
      RawFormDefinition def = defs.get(i);
      String base = "/forms/" + i;

      if (def == null) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_001.wire(), "Form definition entry is empty", base));
        continue;
      }

      // --- topology ---
      String topology = def.topology();
      if (topology == null || topology.isBlank()) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_001.wire(),
                "Form definition requires 'topology' (Phase-0 allowed: " + ALLOWED_TOPOLOGIES + ")",
                base + "/topology"));
      } else if ("parallel".equals(topology)) {
        // Special WKS-FORM-001 message for Phase-1 topology rejection
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_FORM_001.wire(),
                "topology: parallel is a Phase-1 capability — use topology: single",
                base + "/topology"));
      } else if (!ALLOWED_TOPOLOGIES.contains(topology)) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_FORM_001.wire(),
                "Invalid topology '" + topology + "' — Phase-0 allowed: " + ALLOWED_TOPOLOGIES,
                base + "/topology"));
      }

      // --- dataModel ---
      String dataModel = def.dataModel();
      if (dataModel == null || dataModel.isBlank()) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_001.wire(),
                "Form definition requires 'dataModel' (allowed: " + ALLOWED_DATA_MODELS + ")",
                base + "/dataModel"));
      } else if (!ALLOWED_DATA_MODELS.contains(dataModel)) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_008.wire(),
                "Invalid dataModel '" + dataModel + "' — allowed: " + ALLOWED_DATA_MODELS,
                base + "/dataModel"));
      }

      // --- rendering ---
      String rendering = def.rendering();
      if (rendering == null || rendering.isBlank()) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_001.wire(),
                "Form definition requires 'rendering' (allowed: " + ALLOWED_RENDERINGS + ")",
                base + "/rendering"));
      } else if (!ALLOWED_RENDERINGS.contains(rendering)) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_008.wire(),
                "Invalid rendering '" + rendering + "' — allowed: " + ALLOWED_RENDERINGS,
                base + "/rendering"));
      }

      // --- sections (required when dataModel is "sectioned") ---
      // Story 5.3: validate section structure when dataModel declares sectioned layout.
      if ("sectioned".equals(dataModel)) {
        List<RawFormSection> sections = def.sections();
        if (sections == null || sections.isEmpty()) {
          errors.add(
              ErrorDetail.ofField(
                  ErrorCode.WKS_CFG_001.wire(),
                  "Form with dataModel: sectioned must declare at least one section in 'sections[]'",
                  base + "/sections"));
        } else {
          for (int j = 0; j < sections.size(); j++) {
            RawFormSection sec = sections.get(j);
            String secBase = base + "/sections/" + j;
            if (sec == null || sec.id() == null || sec.id().isBlank()) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_CFG_001.wire(),
                      "Section entry at index " + j + " is missing required 'id'",
                      secBase + "/id"));
            }
            if (sec != null && (sec.label() == null || sec.label().isBlank())) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_CFG_001.wire(),
                      "Section '" + sec.id() + "' is missing required 'label'",
                      secBase + "/label"));
            }
          }
        }
      }
    }
  }
}
