package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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

  /**
   * Story 3.4.1 AC5 (finding I7) — bounded LRU cache of historical (caseTypeId, version) ->
   * CaseTypeConfig hydrations. Replaces the unbounded {@code ConcurrentHashMap} that grew without
   * eviction over the application lifetime. Append-only registry semantics make eviction safe: the
   * worst case on a miss is a re-parse via the YAML loader + validator, never stale data.
   *
   * <p>Default capacity 4096 — generous headroom (typical SI carries ~1000 entries lifetime),
   * bounded against pathological hot-reload growth. Override via {@code
   * wks.registry.cache.max-size}.
   *
   * <p>Wrapped in {@link Collections#synchronizedMap}: {@link LinkedHashMap}'s access-order
   * eviction is not thread-safe by itself. The map is on the read-heavy side of {@code findVersion}
   * but write contention is bounded by {@link CaseTypeYamlLoader} parse cost; a single mutex keeps
   * the implementation auditable.
   */
  private final Map<VersionKey, CaseTypeConfig> byVersion;

  private final JsonSchemaGenerator schemaGenerator;

  // Story 3.4 — version-pinned hydration deps. Optional so unit tests that only exercise the
  // in-memory current-version surface can construct the registry without wiring the JPA-backed
  // registry. Field-injected with @Lazy to break the bean-creation cycle: the JPA adapter is in
  // a different package and depends on infrastructure beans, while CaseTypeRegistry is itself
  // wired into ConfigService construction.
  @Autowired(required = false)
  private @Lazy CaseTypeVersionRegistry versionRegistry;

  @Autowired(required = false)
  private @Lazy CaseTypeYamlLoader yamlLoader;

  @Autowired(required = false)
  private @Lazy ConfigValidator configValidator;

  public CaseTypeRegistry(
      JsonSchemaGenerator schemaGenerator,
      @Value("${wks.registry.cache.max-size:4096}") int byVersionMaxSize) {
    this.schemaGenerator = schemaGenerator;
    final int capacity = byVersionMaxSize > 0 ? byVersionMaxSize : 4096;
    // access-order LRU; removeEldestEntry kicks in once size > capacity.
    this.byVersion =
        Collections.synchronizedMap(
            new LinkedHashMap<VersionKey, CaseTypeConfig>(16, 0.75f, true) {
              @Override
              protected boolean removeEldestEntry(Map.Entry<VersionKey, CaseTypeConfig> eldest) {
                return size() > capacity;
              }
            });
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
    // Story 3.4 — when no version row exists yet for this id, materialise a synthetic row at the
    // config's declared version via a minimal YAML stub. This keeps direct callers of the
    // CaseTypeRegistrar (test fixtures that bypass ConfigService) from leaving the version
    // registry empty — which would surface as WKS-VER-001 on the first CaseService.create. The
    // production path (ConfigService.applyVersionRegistry) writes the row BEFORE this call, so
    // the synthetic-stub branch is only exercised by direct-registrar test paths.
    if (versionRegistry != null) {
      versionRegistry
          .currentVersion(config.id())
          .orElseGet(
              () -> {
                String stub = "id: " + config.id() + "\nversion: " + config.version() + "\n";
                versionRegistry.register(
                    config.id(),
                    stub.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "test:in-memory-bypass");
                return config.version();
              });
    }
    Registered incoming = new Registered(config, schemaGenerator.generate(config));
    AtomicReference<RegistrationResult> outcome = new AtomicReference<>();

    byId.compute(
        config.id(),
        (k, existing) -> {
          if (existing == null) {
            outcome.set(RegistrationResult.registered());
            // Story 6.2 — cache byVersion ONLY when the registration is accepted by the
            // older-version guard below. Caching eagerly before the guard would seed a
            // historical-version slot for a config that subsequent guard rules may reject,
            // leaving the version cache inconsistent with byId.
            byVersion.putIfAbsent(new VersionKey(config.id(), config.version()), config);
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
          // Replace path — version is newer than existing; cache it.
          byVersion.putIfAbsent(new VersionKey(config.id(), config.version()), config);
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

  /**
   * Story 3.4 / Decision 20 — exact-version lookup. Hydrates the {@link CaseTypeConfig} from the
   * version registry's stored YAML by re-running the loader + validator pipeline (parse-validate-
   * build, never a serialised blob — the registry persists author YAML, not internal
   * representation). Cached by {@code (id, version)} so re-parse cost is paid once per pinned
   * version.
   *
   * <p>Returns empty when:
   *
   * <ul>
   *   <li>the version dependencies are unavailable (test wiring without the JPA registry);
   *   <li>{@code (id, version)} has no row in {@code case_type_versions} — including the gap window
   *       before Story 3.5's bootstrap migration backfills v1 rows;
   *   <li>the stored YAML fails to re-validate (defensive — should not happen since each row was
   *       written by a successful deploy, but covers schema drift).
   * </ul>
   */
  @Override
  public Optional<CaseTypeConfig> findVersion(String id, int version) {
    if (versionRegistry == null || yamlLoader == null || configValidator == null) {
      return Optional.empty();
    }
    VersionKey key = new VersionKey(id, version);
    CaseTypeConfig cached = byVersion.get(key);
    if (cached != null) {
      return Optional.of(cached);
    }
    return versionRegistry
        .findVersion(id, version)
        .flatMap(
            row -> {
              CaseTypeYamlLoader.RawReadResult read =
                  yamlLoader.readBytes(id + ":v" + version, row.rawYaml());
              if (!read.isParsed()) {
                return Optional.empty();
              }
              ValidationResult vr =
                  configValidator.validate(read.raw(), read.lines(), java.util.Map.of());
              if (vr.isInvalid() || vr.config().isEmpty()) {
                return Optional.empty();
              }
              CaseTypeConfig hydrated = vr.config().get().withVersion(version);
              byVersion.putIfAbsent(key, hydrated);
              return Optional.of(hydrated);
            });
  }

  private record Registered(CaseTypeConfig config, JsonNode schema) {}

  private record VersionKey(String id, int version) {}
}
