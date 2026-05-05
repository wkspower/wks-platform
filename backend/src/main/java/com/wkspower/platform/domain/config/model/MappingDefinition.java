package com.wkspower.platform.domain.config.model;

import java.util.List;

/**
 * Typed, immutable result of {@code MappingValidator.validate(...)} success — the in-memory
 * representation of a CaseType YAML's {@code attachments: [...]} block (Story 4.2 AC1 / AC4 /
 * architecture §786–842, Decision 22). Pure Java — no Spring, no Jackson, no JPA. The only {@code
 * domain/port/} import in this package is {@link
 * com.wkspower.platform.domain.port.BackendSignalKind} (used by {@link
 * AttachmentDefinition.PropertyEmissionRule}).
 *
 * <p>Story 4.2 produces this value object; Story 4.3's {@code BackendSignalRouter} consumes it. 4.2
 * has zero coupling to {@code BackendAdapter} / {@code BackendSignalRouter} (the runtime port from
 * Story 4.1) — the wire shape lives here.
 *
 * <p><b>Zero-attachment case types are first-class</b> (D19 stage-less analogue per architecture
 * §816): every downstream service treats {@link #empty()} identically to a CaseType with no {@code
 * attachments:} key — no branching on absence.
 */
public record MappingDefinition(List<AttachmentDefinition> attachments) {

  private static final MappingDefinition EMPTY = new MappingDefinition(List.of());

  public MappingDefinition {
    attachments = attachments == null ? List.of() : List.copyOf(attachments);
  }

  /**
   * Singleton zero-attachment instance. Used by {@code MappingValidator} when the YAML omits the
   * {@code attachments} key or declares an empty list — both are legal per AC1.
   */
  public static MappingDefinition empty() {
    return EMPTY;
  }
}
