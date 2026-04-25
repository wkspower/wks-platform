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
}
