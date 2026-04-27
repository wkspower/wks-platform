package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.CaseDataValidator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Adapter that bridges {@link JsonSchemaGenerator} (domain-config-driven schema construction) and
 * the networknt JSON Schema validator. Returns the full list of violations — never short-circuits
 * (collect-all invariant). The Jackson {@link ObjectMapper} comes from Story 1.4's {@code
 * JacksonConfig}; the schema factory is built once per JVM (Draft 2020-12).
 *
 * <p>Story 2.5 AC11 #3 — generated {@link JsonSchema} instances are cached by {@code (caseTypeId,
 * version)}. The {@link JsonSchemaGenerator} build + networknt schema compilation are pure
 * functions of the case-type config, so a per-version cache is safe and eliminates the per-call
 * rebuild cost on hot validation paths ({@code POST /api/cases}, {@code PUT /api/cases/\{id\}},
 * future Story 2.7 form submits).
 *
 * <p>The cache is invalidated for a case-type id when {@link ConfigDeployed} fires for that id — a
 * redeploy may bump the version or change the schema shape. The eviction key is the case-type id
 * (all versions are dropped) since the redeployed version will repopulate on its next validate
 * call.
 */
@Component
class CaseDataValidatorAdapter implements CaseDataValidator {

  private static final JsonSchemaFactory SCHEMA_FACTORY =
      JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

  private final JsonSchemaGenerator schemaGenerator;
  private final ObjectMapper objectMapper;

  /**
   * Defensive upper bound on cache size. With per-case-type eviction on {@link ConfigDeployed} the
   * cache normally tops out at one entry per active case type, but a long-running JVM that sees
   * many version bumps without redeploys (or distinct case types created over time) can drift
   * upward. When the bound is hit we flush wholesale — schema generation is cheap on a cold call
   * and this is preferable to an LRU dependency for a Phase-0 surface.
   */
  static final int MAX_CACHE_SIZE = 256;

  private final ConcurrentMap<CacheKey, JsonSchema> schemaCache = new ConcurrentHashMap<>();

  CaseDataValidatorAdapter(JsonSchemaGenerator schemaGenerator, ObjectMapper objectMapper) {
    this.schemaGenerator = schemaGenerator;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<ErrorDetail> validate(CaseTypeConfig caseType, Map<String, Object> data) {
    if (schemaCache.size() > MAX_CACHE_SIZE) {
      schemaCache.clear();
    }
    JsonSchema schema =
        schemaCache.computeIfAbsent(
            new CacheKey(caseType.id(), caseType.version()),
            k -> SCHEMA_FACTORY.getSchema(schemaGenerator.generate(caseType)));
    JsonNode payload = objectMapper.valueToTree(data == null ? Map.of() : data);
    Set<ValidationMessage> messages = schema.validate(payload);

    List<ErrorDetail> errors = new ArrayList<>();
    for (ValidationMessage m : messages) {
      String field = pointerToField(m.getInstanceLocation().toString(), caseType);
      errors.add(ErrorDetail.ofField(ErrorCode.WKS_API_001.wire(), m.getMessage(), field));
    }
    return List.copyOf(errors);
  }

  /**
   * On {@link ConfigDeployed} drop every cached schema for the redeployed case type. A redeploy may
   * rev the version or change the field set — keeping the old compiled schema would silently
   * validate against a stale shape.
   */
  @EventListener
  void onConfigDeployed(ConfigDeployed event) {
    schemaCache.keySet().removeIf(key -> key.caseTypeId().equals(event.caseTypeId()));
  }

  /** Visible for tests — assert cache state without exposing the map. */
  int cacheSize() {
    return schemaCache.size();
  }

  private record CacheKey(String caseTypeId, int version) {}

  /**
   * Resolves a networknt validator instance-location string (either JsonPath-style {@code $.foo} or
   * JSON-Pointer-style {@code /data/foo} / {@code /foo/bar}) to the YAML-declared field id the
   * frontend RHF form references. Phase 0 grammar is flat — there is no nested object schema — but
   * the resolver is defensive against future schema evolution per Story 2.7 AC10.
   *
   * <p>Resolution rules (Story 2.7 AC10):
   *
   * <ol>
   *   <li>Empty / root pointer → wire literal {@code "data"} (form-level violation).
   *   <li>Strip the leading {@code $.} or {@code /} (and a literal {@code data/} or {@code data.}
   *       segment — networknt uses both styles depending on schema-draft and version).
   *   <li>If the remainder matches a top-level field id from the case-type, return it verbatim.
   *   <li>Otherwise return the leaf path token (last {@code /}- or {@code .}-delimited segment) —
   *       belt-and-braces fallback for nested validators that may surface in future drafts. Never
   *       converts {@code /} to {@code .} since YAML field ids may contain {@code _}/{@code -} but
   *       never {@code .}.
   * </ol>
   *
   * <p>Package-private to keep the unit-test surface tight; previously {@code private static}.
   */
  static String pointerToField(String pointer, CaseTypeConfig caseType) {
    if (pointer == null || pointer.isEmpty() || "$".equals(pointer) || "/".equals(pointer)) {
      return "data";
    }
    String stripped = pointer;
    if (stripped.startsWith("$.")) {
      stripped = stripped.substring(2);
    } else if (stripped.startsWith("$")) {
      stripped = stripped.substring(1);
    }
    if (stripped.startsWith("/")) {
      stripped = stripped.substring(1);
    }
    // Strip a leading "data/" or "data." segment when the schema wraps payloads under /data.
    if (stripped.startsWith("data/")) {
      stripped = stripped.substring("data/".length());
    } else if (stripped.startsWith("data.")) {
      stripped = stripped.substring("data.".length());
    } else if ("data".equals(stripped)) {
      return "data";
    }
    if (stripped.isEmpty()) {
      return "data";
    }
    Set<String> declared = declaredFieldIds(caseType);
    if (declared.contains(stripped)) {
      return stripped;
    }
    // Nested or unknown path: take the leaf segment as a best-effort. Only return it when it
    // matches a YAML-declared field id — otherwise fall back to "data" so the frontend renders a
    // form-level banner instead of setError on a non-existent RHF field (which RHF accepts
    // silently, making the error vanish).
    int slash = stripped.lastIndexOf('/');
    int dot = stripped.lastIndexOf('.');
    int cut = Math.max(slash, dot);
    String leaf = cut >= 0 ? stripped.substring(cut + 1) : stripped;
    if (leaf.isEmpty()) {
      return "data";
    }
    return declared.contains(leaf) ? leaf : "data";
  }

  private static Set<String> declaredFieldIds(CaseTypeConfig caseType) {
    if (caseType == null || caseType.fields() == null) {
      return Set.of();
    }
    Set<String> ids = new LinkedHashSet<>();
    for (FieldDefinition f : caseType.fields()) {
      ids.add(f.id());
    }
    return ids;
  }
}
