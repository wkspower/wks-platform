package com.wkspower.platform.domain.config;

/**
 * Outcome of {@link com.wkspower.platform.domain.port.CaseTypeVersionRegistry#register(String,
 * byte[], String)} (Story 3.4 / Decision 20). Parallel in shape to {@link RegistrationResult} but
 * intentionally separate — the version registry has different semantics (monotonic auto-increment,
 * idempotent-by-content-hash, never rejected) than the in-memory CaseTypeRegistry.
 *
 * <p>Pure domain — zero framework imports.
 */
public record CaseTypeVersionRegistration(int version, String hash, Outcome outcome) {

  public enum Outcome {
    /** New row inserted in {@code case_type_versions}. */
    REGISTERED,
    /** Byte-canonically-identical content already present at this version — no insert. */
    IDEMPOTENT
  }

  public static CaseTypeVersionRegistration registered(int version, String hash) {
    return new CaseTypeVersionRegistration(version, hash, Outcome.REGISTERED);
  }

  public static CaseTypeVersionRegistration idempotent(int version, String hash) {
    return new CaseTypeVersionRegistration(version, hash, Outcome.IDEMPOTENT);
  }
}
