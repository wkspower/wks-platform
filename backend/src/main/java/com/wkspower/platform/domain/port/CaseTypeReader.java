package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import java.util.Collection;
import java.util.Optional;

/**
 * Read-side port for case-type configuration. Case-handling code (Story 2.3+) depends on this
 * interface, never the concrete registry — keeps the hexagonal boundary honest and lets tests swap
 * in stub implementations.
 */
public interface CaseTypeReader {

  /** O(1) lookup by id. Returns empty when the case type is unknown or not yet registered. */
  Optional<CaseTypeConfig> find(String id);

  /** Snapshot of all registered configs. Callers must treat the result as immutable. */
  Collection<CaseTypeConfig> all();

  /**
   * Story 3.4 / Decision 20 — exact-version lookup. Hydrates a {@link CaseTypeConfig} from the
   * version registry's stored YAML for in-flight reads (frozen-on-version). Empty when {@code (id,
   * version)} has no row in the registry — including the gap window before Story 3.5's bootstrap
   * migration backfills v1 rows for pre-3.4 CaseTypes.
   *
   * <p>Forms binding (Epic 5) and Mapping binding (Epic 4) inherit frozen-on-version via this
   * method — both resolve via {@code findVersion(case.caseTypeId(), case.caseTypeVersion())}, never
   * via live {@link #find(String)}.
   */
  Optional<CaseTypeConfig> findVersion(String id, int version);
}
