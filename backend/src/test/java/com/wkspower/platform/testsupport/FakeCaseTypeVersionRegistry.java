package com.wkspower.platform.testsupport;

import com.wkspower.platform.domain.config.CaseTypeVersionRecord;
import com.wkspower.platform.domain.config.CaseTypeVersionRegistration;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory fake for {@link CaseTypeVersionRegistry} used by domain-level unit tests (Story 3.4).
 * Mirrors the JPA adapter's contract: hash-keyed idempotence, monotonic auto-increment,
 * append-only.
 *
 * <p>The fake hashes via simple SHA-256 of the raw bytes so a byte-identical re-register is
 * idempotent. Tests that need canonical-YAML semantics (whitespace / comment idempotence) wire the
 * real {@code CaseTypeContentHasher} via the JPA adapter in the IT, not here.
 */
public final class FakeCaseTypeVersionRegistry implements CaseTypeVersionRegistry {

  private final ConcurrentMap<String, List<CaseTypeVersionRecord>> rows = new ConcurrentHashMap<>();

  @Override
  public synchronized CaseTypeVersionRegistration register(
      String caseTypeId, byte[] rawYamlBytes, String publishedBy) {
    return register(caseTypeId, rawYamlBytes, publishedBy, null, null);
  }

  @Override
  public synchronized CaseTypeVersionRegistration register(
      String caseTypeId,
      byte[] rawYamlBytes,
      String publishedBy,
      String bpmnContentHash,
      String mappingHash) {
    String hash = sha256(rawYamlBytes);
    List<CaseTypeVersionRecord> list = rows.computeIfAbsent(caseTypeId, k -> new ArrayList<>());
    for (CaseTypeVersionRecord r : list) {
      if (r.hash().equals(hash)) {
        return CaseTypeVersionRegistration.idempotent(r.version(), hash);
      }
    }
    int next = list.size() + 1;
    list.add(
        new CaseTypeVersionRecord(
            caseTypeId,
            next,
            hash,
            rawYamlBytes.clone(),
            Instant.now(),
            publishedBy,
            bpmnContentHash,
            mappingHash));
    return CaseTypeVersionRegistration.registered(next, hash);
  }

  @Override
  public Optional<Integer> currentVersion(String caseTypeId) {
    List<CaseTypeVersionRecord> list = rows.get(caseTypeId);
    if (list == null || list.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(list.get(list.size() - 1).version());
  }

  @Override
  public Optional<CaseTypeVersionRecord> findVersion(String caseTypeId, int version) {
    List<CaseTypeVersionRecord> list = rows.get(caseTypeId);
    if (list == null) {
      return Optional.empty();
    }
    for (CaseTypeVersionRecord r : list) {
      if (r.version() == version) {
        return Optional.of(r);
      }
    }
    return Optional.empty();
  }

  /** Pre-seed a version (used to drive {@code WKS-VER-001} negative paths in tests). */
  public void seed(String caseTypeId, int version, byte[] yaml) {
    rows.computeIfAbsent(caseTypeId, k -> new ArrayList<>())
        .add(
            new CaseTypeVersionRecord(
                caseTypeId,
                version,
                sha256(yaml),
                yaml.clone(),
                Instant.now(),
                "test:seed",
                null,
                null));
  }

  private static String sha256(byte[] bytes) {
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
      byte[] d = md.digest(bytes);
      StringBuilder sb = new StringBuilder(d.length * 2);
      for (byte b : d) {
        sb.append(String.format("%02x", b & 0xff));
      }
      return sb.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
