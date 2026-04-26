package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.CaseDataValidator;
import java.util.ArrayList;
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
      String field = pointerToField(m.getInstanceLocation().toString());
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

  private static String pointerToField(String pointer) {
    if (pointer == null || pointer.isEmpty() || "$".equals(pointer)) {
      return null;
    }
    // networknt 1.5 uses JsonPath-style locations like "$.applicant_name". Strip the prefix.
    if (pointer.startsWith("$.")) {
      return pointer.substring(2);
    }
    if (pointer.startsWith("/")) {
      return pointer.substring(1);
    }
    return pointer;
  }
}
