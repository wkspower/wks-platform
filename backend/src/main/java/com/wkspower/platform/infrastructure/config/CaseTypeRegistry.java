package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * In-memory hot-reloadable registry for validated case-type configs. Backed by a {@link
 * ConcurrentHashMap} so reads never block and a single-writer {@code register} / {@code replace}
 * swap is atomic at the key.
 *
 * <p>{@code all()} returns an unmodifiable snapshot of an internally cloned list — callers cannot
 * mutate the registry through the return value. {@link #schemaFor(String)} returns the schema
 * generated once per registration; do not regenerate on every lookup.
 */
@Component
public class CaseTypeRegistry implements CaseTypeReader, CaseTypeRegistrar {

  private final ConcurrentMap<String, Registered> byId = new ConcurrentHashMap<>();
  private final JsonSchemaGenerator schemaGenerator;

  public CaseTypeRegistry(JsonSchemaGenerator schemaGenerator) {
    this.schemaGenerator = schemaGenerator;
  }

  @Override
  public Optional<CaseTypeConfig> find(String id) {
    Registered r = byId.get(id);
    return r == null ? Optional.empty() : Optional.of(r.config);
  }

  @Override
  public Collection<CaseTypeConfig> all() {
    List<CaseTypeConfig> snapshot = new ArrayList<>(byId.size());
    for (Registered r : byId.values()) {
      snapshot.add(r.config);
    }
    return Collections.unmodifiableCollection(snapshot);
  }

  /** Returns the cached JSON Schema for {@code id}, or empty when unknown. */
  public Optional<JsonNode> schemaFor(String id) {
    Registered r = byId.get(id);
    return r == null ? Optional.empty() : Optional.of(r.schema);
  }

  /**
   * Register-or-replace with version semantics. Exposed via {@link CaseTypeRegistrar} so
   * domain-layer {@code ConfigService} can drive the write side without importing the concrete
   * registry.
   */
  @Override
  public RegistrationResult register(CaseTypeConfig config) {
    Registered incoming = new Registered(config, schemaGenerator.generate(config));
    Registered existing = byId.get(config.id());

    if (existing == null) {
      Registered prior = byId.putIfAbsent(config.id(), incoming);
      if (prior == null) {
        return RegistrationResult.registered();
      }
      // Lost the race — fall through to version check against whatever landed.
      existing = prior;
    }

    if (config.version() == existing.config.version()) {
      return RegistrationResult.idempotent();
    }
    if (config.version() < existing.config.version()) {
      return RegistrationResult.rejectedOlderVersion(
          ErrorDetail.ofField(
              ErrorCode.WKS_CFG_011.wire(),
              "Incoming version "
                  + config.version()
                  + " is older than registered version "
                  + existing.config.version()
                  + " for id '"
                  + config.id()
                  + "'",
              "version"));
    }
    // Atomic swap at the key.
    byId.put(config.id(), incoming);
    return RegistrationResult.replaced();
  }

  /** Package-private removal — not part of the read port. */
  void remove(String id) {
    byId.remove(id);
  }

  private record Registered(CaseTypeConfig config, JsonNode schema) {}
}
