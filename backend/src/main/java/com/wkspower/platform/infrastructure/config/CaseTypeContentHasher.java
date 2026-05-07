package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Canonical content hasher for CaseType YAML (Story 3.4 / Decision 20). Single source of truth for
 * the algorithm — never inline this in the registry adapter or anywhere else.
 *
 * <p>Algorithm (locked Q2 2026-05-06):
 *
 * <ol>
 *   <li>Parse the raw YAML bytes via Jackson's YAML mapper into a generic {@link JsonNode} tree.
 *   <li>Serialise the tree back to JSON via an {@link ObjectMapper} with {@link
 *       MapperFeature#SORT_PROPERTIES_ALPHABETICALLY} and {@link
 *       SerializationFeature#ORDER_MAP_ENTRIES_BY_KEYS} enabled, producing a canonical UTF-8 byte
 *       stream that is invariant under whitespace edits, comment edits, and key reordering.
 *   <li>SHA-256 the canonical bytes; lowercase hex output (64 chars).
 * </ol>
 *
 * <p>The canonical JSON is hash-only — operators read the raw YAML from {@code
 * case_type_versions.definition_yaml} (Q3 LOCKED). Storing the canonical form would lose comments
 * and formatting that authors rely on.
 */
public final class CaseTypeContentHasher {

  private static final YAMLMapper YAML = new YAMLMapper();

  private static final ObjectMapper CANONICAL_JSON =
      new ObjectMapper()
          .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
          .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

  /**
   * Story 4.5 AC3 — Compute the SHA-256 hex hash of raw bytes directly (no YAML canonicalization).
   * Used for BPMN content hashing where the raw byte identity is the fingerprint — the BPMN file is
   * stored verbatim and there is no canonical normalisation step. Public to allow test wiring via
   * method reference ({@code CaseTypeContentHasher::hashBytes}) in domain-layer unit tests.
   *
   * @throws IllegalArgumentException if {@code rawBytes} is null or empty
   */
  public static String hashBytes(byte[] rawBytes) {
    if (rawBytes == null || rawBytes.length == 0) {
      throw new IllegalArgumentException(
          "CaseTypeContentHasher.hashBytes: rawBytes must be non-null and non-empty");
    }
    return sha256Hex(rawBytes);
  }

  /**
   * Compute the canonical SHA-256 hex hash of the supplied YAML bytes.
   *
   * @throws IllegalArgumentException if {@code rawYamlBytes} is null, empty, or unparseable as YAML
   */
  public String hash(byte[] rawYamlBytes) {
    if (rawYamlBytes == null || rawYamlBytes.length == 0) {
      throw new IllegalArgumentException(
          "CaseTypeContentHasher: rawYamlBytes must be non-null and non-empty");
    }
    Object parsed;
    try {
      parsed = YAML.readValue(rawYamlBytes, Object.class);
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "CaseTypeContentHasher: YAML parse failure — " + e.getMessage(), e);
    }
    if (parsed == null) {
      throw new IllegalArgumentException("CaseTypeContentHasher: YAML document is empty or null");
    }
    Object canonical = canonicalize(parsed);
    byte[] canonicalJson;
    try {
      canonicalJson = CANONICAL_JSON.writeValueAsBytes(canonical);
    } catch (IOException e) {
      throw new IllegalStateException(
          "CaseTypeContentHasher: canonical serialisation failed (should never happen)", e);
    }
    return sha256Hex(canonicalJson);
  }

  /**
   * Recursively rebuild the parsed structure with {@link java.util.TreeMap} for any {@code Map}
   * level so map keys are emitted in deterministic alphabetical order regardless of YAML source
   * formatting. {@code List}s preserve element order — list ordering IS semantic and must hash
   * differently when authors reorder array elements.
   */
  private static Object canonicalize(Object value) {
    if (value instanceof java.util.Map<?, ?> m) {
      java.util.TreeMap<String, Object> sorted = new java.util.TreeMap<>();
      for (java.util.Map.Entry<?, ?> e : m.entrySet()) {
        sorted.put(String.valueOf(e.getKey()), canonicalize(e.getValue()));
      }
      return sorted;
    }
    if (value instanceof java.util.List<?> l) {
      java.util.List<Object> out = new java.util.ArrayList<>(l.size());
      for (Object item : l) {
        out.add(canonicalize(item));
      }
      return out;
    }
    return value;
  }

  private static String sha256Hex(byte[] bytes) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable in this JVM", e);
    }
    byte[] digest = md.digest(bytes);
    StringBuilder sb = new StringBuilder(digest.length * 2);
    for (byte b : digest) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }

  // Charset reference kept for clarity even though Jackson handles UTF-8 internally.
  @SuppressWarnings("unused")
  private static final java.nio.charset.Charset CANONICAL_CHARSET = StandardCharsets.UTF_8;
}
