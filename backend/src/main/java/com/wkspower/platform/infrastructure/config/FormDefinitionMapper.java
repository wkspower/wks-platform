package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldOption;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.FormSection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps {@link RawFormDefinition} transport objects to {@link FormDefinition} domain records.
 *
 * <p>Story 5.2 — called from {@link ConfigValidator} after form validation succeeds. The raw {@code
 * fields[]} entries are {@code Map<String, Object>} deserialized from YAML; this mapper extracts
 * the same slots that {@link RawCaseTypeConfig.RawField} carries so downstream consumers see a
 * uniform {@link FieldDefinition} shape regardless of whether the field originated from the
 * case-type-level {@code fields[]} or a form-level {@code fields[]} list.
 *
 * <p>Unknown keys in the raw map are silently ignored for forward-compatibility (mirrors the
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} posture on {@link RawCaseTypeConfig}).
 *
 * <p>This mapper is I/O-free and stateless — safe to call from a Spring {@code @Component} or a
 * plain unit test.
 */
public final class FormDefinitionMapper {

  private FormDefinitionMapper() {
    // utility
  }

  /**
   * Convert one {@link RawFormDefinition} (already validated by {@link FormValidator}) to a {@link
   * FormDefinition}.
   */
  public static FormDefinition toDomain(RawFormDefinition raw) {
    List<FieldDefinition> fields = mapFields(raw.fields());
    List<FormSection> sections = mapSections(raw.sections());
    return new FormDefinition(
        raw.id(), raw.topology(), raw.dataModel(), raw.rendering(), fields, sections);
  }

  /**
   * Story 5.3 — map raw section entries to domain {@link FormSection} records. Delegates to the
   * existing {@link #mapFields} helper for each section's field list.
   */
  private static List<FormSection> mapSections(List<RawFormSection> rawSections) {
    if (rawSections == null || rawSections.isEmpty()) return List.of();
    List<FormSection> out = new ArrayList<>(rawSections.size());
    for (RawFormSection s : rawSections) {
      if (s == null || s.id() == null || s.id().isBlank()) continue;
      String label = s.label() != null ? s.label() : s.id();
      List<FieldDefinition> sectionFields = mapFields(s.fields());
      out.add(new FormSection(s.id(), label, sectionFields));
    }
    return List.copyOf(out);
  }

  // ---- private helpers -----------------------------------------------------------------

  @SuppressWarnings("unchecked")
  private static List<FieldDefinition> mapFields(List<Map<String, Object>> rawFields) {
    if (rawFields == null || rawFields.isEmpty()) {
      return List.of();
    }
    List<FieldDefinition> out = new ArrayList<>(rawFields.size());
    int index = 0;
    for (Map<String, Object> m : rawFields) {
      if (m == null) {
        index++;
        continue;
      }
      String id = string(m, "id");
      if (id == null || id.isBlank()) {
        index++;
        continue; // missing id — validator should have caught this; skip defensively
      }
      String displayName = string(m, "displayName");
      if (displayName == null) displayName = id;

      String typeStr = string(m, "type");
      // P11 — fail-fast on unknown field type: silently defaulting to TEXT accepts typos without
      // surfacing them, causing the form to render the wrong widget at runtime. Config authoring
      // errors must surface immediately at deploy time (same posture as WKS-CFG-002 for case-type
      // fields). FormValidator should have already rejected unknown types; this guard is a
      // defensive belt-and-suspenders in case the mapper is called outside the validator path.
      FieldType type;
      if (typeStr == null || typeStr.isBlank()) {
        type = FieldType.TEXT; // no type declared — validator catches this; default defensively
      } else {
        type =
            FieldType.fromWire(typeStr)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Unknown field type '"
                                + typeStr
                                + "' on field '"
                                + id
                                + "' — check the form YAML (valid types: text, textarea, number,"
                                + " date, select, checkbox, file)"));
      }

      boolean required = booleanValue(m, "required");
      boolean requiredOnCreate =
          m.containsKey("requiredOnCreate") ? booleanValue(m, "requiredOnCreate") : required;

      Object orderRaw = m.get("order");
      int order = orderRaw instanceof Number ? ((Number) orderRaw).intValue() : index;

      // Options (select fields)
      List<FieldOption> options = List.of();
      Object optRaw = m.get("options");
      if (optRaw instanceof List<?> optList) {
        List<FieldOption> opts = new ArrayList<>();
        for (Object o : optList) {
          if (o instanceof Map<?, ?> om) {
            String label = string((Map<String, Object>) om, "label");
            String value = string((Map<String, Object>) om, "value");
            if (label != null && value != null) {
              opts.add(new FieldOption(label, value));
            }
          }
        }
        options = List.copyOf(opts);
      }

      // Type-specific slots
      FieldDefinition.TypeSlots slots = mapSlots(m);

      out.add(
          new FieldDefinition(
              id, displayName, type, required, requiredOnCreate, order, options, slots));
      index++;
    }
    return List.copyOf(out);
  }

  private static FieldDefinition.TypeSlots mapSlots(Map<String, Object> m) {
    Integer minLength = intValue(m, "minLength");
    Integer maxLength = intValue(m, "maxLength");
    Double min = doubleValue(m, "min");
    Double max = doubleValue(m, "max");
    Double step = doubleValue(m, "step");
    String dateMin = string(m, "dateMin");
    String dateMax = string(m, "dateMax");
    Long maxBytes = longValue(m, "maxBytes");

    @SuppressWarnings("unchecked")
    List<String> allowedMimeTypes =
        m.get("allowedMimeTypes") instanceof List<?>
            ? (List<String>) m.get("allowedMimeTypes")
            : List.of();

    if (minLength == null
        && maxLength == null
        && min == null
        && max == null
        && step == null
        && dateMin == null
        && dateMax == null
        && maxBytes == null
        && allowedMimeTypes.isEmpty()) {
      return null;
    }
    return new FieldDefinition.TypeSlots(
        minLength, maxLength, min, max, step, dateMin, dateMax, maxBytes, allowedMimeTypes);
  }

  private static String string(Map<?, ?> m, String key) {
    Object v = m.get(key);
    return v instanceof String s ? s : null;
  }

  private static boolean booleanValue(Map<?, ?> m, String key) {
    Object v = m.get(key);
    if (v instanceof Boolean b) return b;
    if (v instanceof String s) return Boolean.parseBoolean(s);
    return false;
  }

  private static Integer intValue(Map<?, ?> m, String key) {
    Object v = m.get(key);
    if (v instanceof Number n) return n.intValue();
    return null;
  }

  private static Long longValue(Map<?, ?> m, String key) {
    Object v = m.get(key);
    if (v instanceof Number n) return n.longValue();
    return null;
  }

  private static Double doubleValue(Map<?, ?> m, String key) {
    Object v = m.get(key);
    if (v instanceof Number n) return n.doubleValue();
    return null;
  }
}
