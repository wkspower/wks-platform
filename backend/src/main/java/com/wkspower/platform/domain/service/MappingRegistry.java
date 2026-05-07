package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.port.CaseTypeRef;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Story 4.3 AC3 — runtime-side index from {@code (caseTypeId, version)} to the validated {@link
 * MappingDefinition} produced at deploy time by Story 4.2's {@code MappingValidator}. Pure-Java —
 * no Spring annotations on the class itself; wired as a singleton {@code @Bean} from {@code
 * infrastructure.config.WorkflowAdapterConfig} (NFR36 — domain has zero framework imports).
 *
 * <p>Backed by a {@link ConcurrentHashMap} for read-heavy concurrent access (mirrors {@link
 * WorkflowAdapterBinder}'s pattern). Reads never block; the single-writer {@code register} / {@code
 * unregister} swap is atomic at the key.
 *
 * <p>{@link MappingDefinition#empty()} is a legal value — registered for every CaseType version
 * whose YAML omits {@code attachments:} or declares {@code attachments: []}. Architecture D22 +
 * D19's "stage-less paths must remain unbranched" — the router treats empty mapping identically to
 * "no rule matches", surfacing {@code WKS-MAP-404} on any signal arrival.
 *
 * <p><b>Frozen-on-version pin (D20):</b> the lookup key is {@code (caseTypeId, version)}; the
 * router uses the CaseInstance's pinned version, never the latest deployed version. Hot-reload (D3)
 * re-runs {@link #register} for the new version while in-flight cases continue resolving against
 * their pinned older version.
 */
public class MappingRegistry {

  private final ConcurrentMap<MappingKey, MappingDefinition> byKey = new ConcurrentHashMap<>();

  /**
   * Register {@code definition} as the active mapping for {@code (caseType.caseTypeId(), version)}.
   * The {@code version} parameter is treated as opaque (D20) — same semantics as {@link
   * CaseTypeRef#version()}.
   *
   * <p>Same-key replacement is allowed (hot-reload) — the most recent registration wins.
   */
  public void register(CaseTypeRef caseType, String version, MappingDefinition definition) {
    Objects.requireNonNull(caseType, "caseType");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(definition, "definition");
    byKey.put(new MappingKey(caseType.caseTypeId(), version), definition);
  }

  /**
   * Drop the registered mapping for {@code (caseType.caseTypeId(), version)}. Idempotent on unknown
   * / already-removed keys.
   */
  public void unregister(CaseTypeRef caseType, String version) {
    Objects.requireNonNull(caseType, "caseType");
    Objects.requireNonNull(version, "version");
    byKey.remove(new MappingKey(caseType.caseTypeId(), version));
  }

  /**
   * Resolve the {@link MappingDefinition} bound to {@code (caseType.caseTypeId(), version)}. Empty
   * when the key was never registered — the router translates that into {@code WKS-MAP-404} with
   * detail {@code caseTypeVersion not in registry}.
   */
  public Optional<MappingDefinition> resolve(CaseTypeRef caseType, String version) {
    Objects.requireNonNull(caseType, "caseType");
    Objects.requireNonNull(version, "version");
    return Optional.ofNullable(byKey.get(new MappingKey(caseType.caseTypeId(), version)));
  }

  /** Composite lookup key — kept private since callers only see the {@link CaseTypeRef} surface. */
  private record MappingKey(String caseTypeId, String version) {
    private MappingKey {
      Objects.requireNonNull(caseTypeId, "caseTypeId");
      Objects.requireNonNull(version, "version");
    }
  }
}
