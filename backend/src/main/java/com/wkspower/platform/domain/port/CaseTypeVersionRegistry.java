package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.config.CaseTypeVersionRecord;
import com.wkspower.platform.domain.config.CaseTypeVersionRegistration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain-side port for the immutable, append-only CaseType version registry (Story 3.4 / Decision
 * 20). The {@code case_type_versions} table is the durable record; this port abstracts the JPA
 * adapter so {@code ConfigService} stays framework-free.
 *
 * <p>Contract:
 *
 * <ul>
 *   <li>{@link #register(String, byte[], String)} computes a canonical SHA-256 over {@code
 *       rawYamlBytes}; if a row already exists for {@code (caseTypeId, hash)} the call is
 *       idempotent and returns the existing version. Otherwise a new row is inserted at {@code
 *       max(version)+1} (or {@code 1} if first deploy) and {@link
 *       CaseTypeVersionRegistration.Outcome#REGISTERED} is returned.
 *   <li>{@link #currentVersion(String)} returns the highest assigned version for {@code
 *       caseTypeId}, empty when the registry has no row for the id.
 *   <li>{@link #findVersion(String, int)} returns the immutable snapshot for an exact {@code
 *       (caseTypeId, version)} key — the durable record for in-flight cases reading the schema they
 *       bound to (Decision 20: frozen-on-version).
 * </ul>
 *
 * <p>This port is the sole binding seam for in-flight cases. Forms binding (Epic 5) and Mapping
 * binding (Epic 4) inherit frozen-on-version via {@link CaseTypeReader#findVersion(String, int)};
 * that read path delegates here for the raw YAML, then re-runs the loader pipeline.
 */
public interface CaseTypeVersionRegistry {

  /** Interface-level logger for default-method warnings (SLF4J allows this pattern). */
  Logger log = LoggerFactory.getLogger(CaseTypeVersionRegistry.class);

  /**
   * Register or short-circuit a CaseType deployment. The canonical hash of {@code rawYamlBytes}
   * decides:
   *
   * <ul>
   *   <li>existing row at {@code (caseTypeId, hash)} → IDEMPOTENT, returning that version
   *   <li>otherwise → INSERT row at next-monotonic version, returning REGISTERED
   * </ul>
   *
   * @param caseTypeId case-type id (non-blank, ≤ 64 chars)
   * @param rawYamlBytes author-supplied YAML bytes (non-null, non-empty)
   * @param publishedBy actorId for REST-driven deploys, or {@code "system:startup"} for the boot
   *     loader
   */
  CaseTypeVersionRegistration register(String caseTypeId, byte[] rawYamlBytes, String publishedBy);

  /**
   * Story 4.5 AC3 — register with deployment fingerprints. Extends the 3-arg overload with BPMN
   * content hash and mapping hash stored in the {@code case_type_versions} row for forensic /
   * integrity purposes (Decision 22). Both may be {@code null} for zero-attachment deploys (D22
   * first-class).
   *
   * @param bpmnContentHash SHA-256 hex of raw BPMN bytes, or {@code null}
   * @param mappingHash SHA-256 hex of canonical {@code MappingDefinition.toString()}, or {@code
   *     null}
   */
  default CaseTypeVersionRegistration register(
      String caseTypeId,
      byte[] rawYamlBytes,
      String publishedBy,
      String bpmnContentHash,
      String mappingHash) {
    // Default delegates to the 3-arg overload for adapters that have not yet been updated.
    // Production adapter (CaseTypeVersionRegistryAdapter) overrides this to persist the hashes.
    //
    // P9 — warn when the default is invoked so that implementors that haven't overridden the
    // full 5-arg signature are visible in the logs. This avoids silent fingerprint loss.
    // TODO(Story 4.6): remove this default and force all implementors to implement the full
    //   signature; the default is kept here to avoid breaking test fakes in the short term.
    log.warn(
        "CaseTypeVersionRegistry.register 5-arg default called — fingerprints discarded;"
            + " override the full signature (caseTypeId={})",
        caseTypeId);
    return register(caseTypeId, rawYamlBytes, publishedBy);
  }

  /**
   * Highest assigned version for {@code caseTypeId}, empty when no row exists. Used by {@link
   * com.wkspower.platform.domain.service.CaseService#create} to bind a new case to the registry's
   * current version.
   */
  Optional<Integer> currentVersion(String caseTypeId);

  /**
   * Exact-version lookup for in-flight reads (Decision 20 frozen-on-version). Returns the immutable
   * row including the raw YAML; callers re-parse on demand.
   */
  Optional<CaseTypeVersionRecord> findVersion(String caseTypeId, int version);
}
