package com.wkspower.platform.domain.config.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

  /**
   * Story 4.5 AC3 — compute a stable SHA-256 fingerprint for this mapping definition. The
   * fingerprint is derived from {@link Object#toString()} of this record (which serializes all
   * fields transitively via the auto-generated record {@code toString()}), then SHA-256d. Pure JDK
   * — no Jackson, no YAML, no Spring — respecting NFR36 and the ArchUnit constraint that {@code
   * MappingDefinition} has no framework imports.
   *
   * <p>Returns {@code null} when {@code attachments} is empty — the caller stores {@code NULL} in
   * {@code case_type_versions.mapping_hash} for zero-attachment deploys (D22: zero-attachment is
   * first-class).
   *
   * <p>The hash is a forensic / integrity column, not a routing key. Stability guarantee: for the
   * same logical mapping content, {@link #toString()} produces the same string because Java records
   * auto-generate a deterministic {@code toString()} that reflects all component fields in
   * declaration order.
   */
  public String computeHash() {
    if (attachments.isEmpty()) {
      return null;
    }
    byte[] input = this.toString().getBytes(StandardCharsets.UTF_8);
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable in this JVM", e);
    }
    byte[] digest = md.digest(input);
    StringBuilder sb = new StringBuilder(digest.length * 2);
    for (byte b : digest) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }
}
