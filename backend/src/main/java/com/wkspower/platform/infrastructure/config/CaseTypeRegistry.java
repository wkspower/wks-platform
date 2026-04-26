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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
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
    Objects.requireNonNull(config, "config");
    Registered incoming = new Registered(config, schemaGenerator.generate(config));
    AtomicReference<RegistrationResult> outcome = new AtomicReference<>();

    byId.compute(
        config.id(),
        (k, existing) -> {
          if (existing == null) {
            outcome.set(RegistrationResult.registered());
            return incoming;
          }
          if (config.version() == existing.config.version()) {
            outcome.set(RegistrationResult.idempotent());
            return existing;
          }
          if (config.version() < existing.config.version()) {
            outcome.set(
                RegistrationResult.rejectedOlderVersion(
                    ErrorDetail.ofField(
                        ErrorCode.WKS_CFG_011.wire(),
                        "Incoming version "
                            + config.version()
                            + " is older than registered version "
                            + existing.config.version()
                            + " for id '"
                            + config.id()
                            + "'",
                        "version")));
            return existing;
          }
          outcome.set(RegistrationResult.replaced());
          return incoming;
        });
    return outcome.get();
  }

  /**
   * Equivalent to {@link #register(CaseTypeConfig)} — exposed under the AC7 literal name {@code
   * replace}. The version compare-and-swap semantics are identical: same-version is idempotent,
   * lower-version is rejected with {@code WKS-CFG-011}.
   */
  public RegistrationResult replace(CaseTypeConfig config) {
    return register(config);
  }

  /** Removal exposed via {@link CaseTypeRegistrar} — used by deploy rollback. */
  @Override
  public void remove(String id) {
    byId.remove(id);
  }

  private record Registered(CaseTypeConfig config, JsonNode schema) {}
}
