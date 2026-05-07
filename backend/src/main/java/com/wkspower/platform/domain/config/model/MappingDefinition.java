package com.wkspower.platform.domain.config.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
   * Story 4.5 AC3 — compute a stable SHA-256 fingerprint for this mapping definition.
   *
   * <p>The fingerprint is derived from a <em>canonical</em> string representation of the mapping
   * content where all {@code Map} fields are serialised in sorted key order. This guarantees
   * stability across JVM restarts, regardless of the internal iteration order of {@link
   * java.util.Map#copyOf} (which uses a hash-based implementation that does NOT guarantee
   * consistent iteration order across JVM instances).
   *
   * <p>Pure JDK — no Jackson, no YAML, no Spring — respecting NFR36 and the ArchUnit constraint
   * ({@code mappingDomainModelHasNoFrameworkImports}) that forbids {@code com.fasterxml.jackson..}
   * imports in {@code MappingDefinition}.
   *
   * <p>Returns {@code null} when {@code attachments} is empty — the caller stores {@code NULL} in
   * {@code case_type_versions.mapping_hash} for zero-attachment deploys (D22: zero-attachment is
   * first-class).
   */
  public String computeHash() {
    if (attachments.isEmpty()) {
      return null;
    }
    byte[] input = canonicalString().getBytes(StandardCharsets.UTF_8);
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

  /**
   * Build a deterministic string from this record's content. All {@code Map} values are rebuilt as
   * {@link TreeMap} (sorted by key) before being serialised so that JVM-restart hash-map iteration
   * differences do not produce different fingerprints for the same logical content. Lists preserve
   * their declared order (list ordering IS semantic).
   */
  private String canonicalString() {
    StringBuilder sb = new StringBuilder("MappingDefinition[attachments=");
    sb.append('[');
    for (int i = 0; i < attachments.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      appendCanonical(sb, attachments.get(i));
    }
    sb.append(']');
    sb.append(']');
    return sb.toString();
  }

  private static void appendCanonical(StringBuilder sb, AttachmentDefinition a) {
    sb.append("AttachmentDefinition[type=").append(a.type());
    sb.append(", file=").append(a.file());
    sb.append(", scope=").append(a.scope());
    sb.append(", stageScopeId=").append(a.stageScopeId());
    sb.append(", userTaskMappings=").append(sortedMapString(a.userTaskMappings()));
    sb.append(", endEventMapping=").append(a.endEventMapping());
    sb.append(", signalMappings=").append(sortedMapString(a.signalMappings()));
    sb.append(", propertyEmissionRules=").append(a.propertyEmissionRules());
    sb.append(']');
  }

  private static <V> String sortedMapString(Map<String, V> map) {
    if (map.isEmpty()) {
      return "{}";
    }
    TreeMap<String, V> sorted = new TreeMap<>(map);
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, V> e : sorted.entrySet()) {
      if (!first) {
        sb.append(", ");
      }
      sb.append(e.getKey()).append('=').append(e.getValue());
      first = false;
    }
    sb.append('}');
    return sb.toString();
  }
}
