package com.wkspower.platform.domain.config.model;

/**
 * Story 5.6 AC4 — Top-level case-type setting that controls the default editability of fields which
 * omit an explicit {@code editableBy} declaration.
 *
 * <ul>
 *   <li>{@link #EDITABLE_BY_DEFAULT} (default): a field with no {@code editableBy} is editable for
 *       every authenticated user. Preserves pre-5.6 behavior — zero regression on existing seed
 *       YAMLs.
 *   <li>{@link #LOCKED_BY_DEFAULT}: a field with no {@code editableBy} is read-only for every user.
 *       Server-side {@code submitForm} rejects any change to such fields with {@code
 *       WKS-AUTHZ-001}; renderers disable the input with a "locked — no editableBy declaration"
 *       tooltip.
 * </ul>
 *
 * <p>This decision is reversible by a one-line default flip on {@code RawCaseTypeConfig} + {@code
 * CaseTypeConfig} if Reading B (lenient — see story file §AC4) becomes the right call.
 */
public enum DefaultFieldEditability {
  EDITABLE_BY_DEFAULT,
  LOCKED_BY_DEFAULT;

  /**
   * Parse the YAML wire string into the enum. {@code null} or blank returns {@link
   * #EDITABLE_BY_DEFAULT} (the safe default-on-omit). Unknown values throw {@link
   * IllegalArgumentException} — the validator catches and emits {@code WKS-FORM-001} per AC4.
   */
  public static DefaultFieldEditability fromYaml(String s) {
    if (s == null || s.isBlank()) return EDITABLE_BY_DEFAULT;
    return switch (s) {
      case "editable-by-default" -> EDITABLE_BY_DEFAULT;
      case "locked-by-default" -> LOCKED_BY_DEFAULT;
      default ->
          throw new IllegalArgumentException(
              "Unknown defaultFieldEditability '"
                  + s
                  + "' — must be 'editable-by-default' or 'locked-by-default'");
    };
  }

  /** Wire string for the YAML / DTO surface — kebab-case. */
  public String wire() {
    return switch (this) {
      case EDITABLE_BY_DEFAULT -> "editable-by-default";
      case LOCKED_BY_DEFAULT -> "locked-by-default";
    };
  }
}
