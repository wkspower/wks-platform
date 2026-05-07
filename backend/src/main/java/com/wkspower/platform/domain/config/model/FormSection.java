package com.wkspower.platform.domain.config.model;

import java.util.List;

/**
 * One section entry in a {@link FormDefinition} with {@code dataModel: sectioned}. Immutable.
 *
 * <p>Story 5.3 — groups a set of fields under a labelled expandable panel on the frontend.
 */
public record FormSection(String id, String label, List<FieldDefinition> fields) {

  public FormSection {
    fields = fields == null ? List.of() : List.copyOf(fields);
  }
}
