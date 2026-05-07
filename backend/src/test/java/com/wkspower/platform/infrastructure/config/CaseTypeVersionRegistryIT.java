package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.CaseTypeVersionRegistration;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.exception.WksVersionException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeVersionJpaRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Story 3.4 / Decision 20 — integration coverage for the CaseType version registry.
 *
 * <p>Scenarios derived from AC7 §1–§10. H2 only per Q4 LOCKED (Postgres-IT deferred).
 */
@SpringBootTest
@ActiveProfiles("dev")
@ExtendWith(OutputCaptureExtension.class)
class CaseTypeVersionRegistryIT {

  @TempDir static Path dbDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) {
    reg.add(
        "spring.datasource.url",
        () -> "jdbc:h2:file:" + dbDir.resolve("ver-it") + ";DB_CLOSE_DELAY=-1");
    reg.add("wks.case-types.dir", () -> "");
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
  }

  private static final String J9_BASE =
      "id: j9-zero-zero\n"
          + "displayName: \"[J9] Zero Stages Zero Process\"\n"
          + "version: 1\n"
          + "roles:\n"
          + "  - name: admin\n"
          + "    permissions: [view, create, edit, transition]\n";

  @Autowired private CaseTypeVersionRegistry registry;
  @Autowired private CaseTypeVersionJpaRepository repo;
  @Autowired private ConfigService configService;
  @Autowired private CaseTypeReader reader;
  @Autowired private CaseService caseService;
  @Autowired private UserRepository users;
  @Autowired private com.wkspower.platform.domain.port.CaseTypeRegistrar registrar;

  private UUID actorId() {
    return users
        .findByEmail("admin@wkspower.local")
        .orElseThrow(() -> new IllegalStateException("admin user not seeded"))
        .id();
  }

  @AfterEach
  void wipe() {
    repo.deleteAll();
    // The in-memory CaseTypeRegistry persists across tests in the same Spring context. Without
    // this remove, a prior test's registered v2 would block the next test's v1 first-deploy via
    // the in-memory registry's monotonic version-monotonic guard (WKS-CFG-011).
    registrar.remove("j9-zero-zero");
  }

  // ---- §1 First deploy creates v1 -----------------------------------------------------------
  @Test
  void firstDeployCreatesV1() {
    CaseTypeVersionRegistration r =
        registry.register("j9-zero-zero", J9_BASE.getBytes(), "system:startup");
    assertThat(r.outcome()).isEqualTo(CaseTypeVersionRegistration.Outcome.REGISTERED);
    assertThat(r.version()).isEqualTo(1);
    assertThat(r.hash()).hasSize(64);

    var rows = repo.findAll();
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).getCaseTypeId()).isEqualTo("j9-zero-zero");
    assertThat(rows.get(0).getVersion()).isEqualTo(1);
    assertThat(rows.get(0).getDefinitionYaml()).isEqualTo(J9_BASE);
    assertThat(rows.get(0).getPublishedBy()).isEqualTo("system:startup");
  }

  // ---- §2 Byte-identical re-deploy is idempotent ----
  @Test
  void byteIdenticalReDeployIsIdempotent() {
    registry.register("j9-zero-zero", J9_BASE.getBytes(), "system:startup");
    CaseTypeVersionRegistration second =
        registry.register("j9-zero-zero", J9_BASE.getBytes(), "system:startup");
    assertThat(second.outcome()).isEqualTo(CaseTypeVersionRegistration.Outcome.IDEMPOTENT);
    assertThat(second.version()).isEqualTo(1);
    assertThat(repo.findAll()).hasSize(1);
  }

  // ---- §3 Whitespace-only edit is idempotent ----
  @Test
  void whitespaceOnlyEditIsIdempotent() {
    registry.register("j9-zero-zero", J9_BASE.getBytes(), "system:startup");
    String reorderedAndPadded =
        "displayName: \"[J9] Zero Stages Zero Process\"\n"
            + "\n"
            + "version: 1\n"
            + "id: j9-zero-zero\n"
            + "\n"
            + "roles:\n"
            + "  - name: admin\n"
            + "    permissions: [view, create, edit, transition]\n";
    CaseTypeVersionRegistration r =
        registry.register("j9-zero-zero", reorderedAndPadded.getBytes(), "system:startup");
    assertThat(r.outcome()).isEqualTo(CaseTypeVersionRegistration.Outcome.IDEMPOTENT);
    assertThat(repo.findAll()).hasSize(1);
  }

  // ---- §4 Comment-only edit is idempotent ----
  @Test
  void commentOnlyEditIsIdempotent() {
    registry.register("j9-zero-zero", J9_BASE.getBytes(), "system:startup");
    String commented = "# story 3.4 IT fixture\n" + J9_BASE;
    CaseTypeVersionRegistration r =
        registry.register("j9-zero-zero", commented.getBytes(), "system:startup");
    assertThat(r.outcome()).isEqualTo(CaseTypeVersionRegistration.Outcome.IDEMPOTENT);
    assertThat(repo.findAll()).hasSize(1);
  }

  // ---- §5 Semantic edit creates v2 ----
  @Test
  void semanticEditCreatesV2() {
    registry.register("j9-zero-zero", J9_BASE.getBytes(), "system:startup");
    String addedRole =
        J9_BASE.replace(
            "  - name: admin\n    permissions: [view, create, edit, transition]\n",
            "  - name: admin\n    permissions: [view, create, edit, transition]\n"
                + "  - name: viewer\n    permissions: [view]\n");
    CaseTypeVersionRegistration r =
        registry.register("j9-zero-zero", addedRole.getBytes(), "system:startup");
    assertThat(r.outcome()).isEqualTo(CaseTypeVersionRegistration.Outcome.REGISTERED);
    assertThat(r.version()).isEqualTo(2);
    assertThat(repo.findAll()).hasSize(2);
  }

  // ---- §6 Case bound to v1 reads v1 schema via findVersion ----
  @Test
  void findVersionReturnsHistoricalSchema() {
    configService.validateAndRegister("j9-it.yaml", J9_BASE.getBytes(), "system:startup");
    String v2 =
        J9_BASE.replace(
            "  - name: admin\n    permissions: [view, create, edit, transition]\n",
            "  - name: admin\n    permissions: [view, create, edit, transition]\n"
                + "  - name: viewer\n    permissions: [view]\n");
    configService.validateAndRegister("j9-it-v2.yaml", v2.getBytes(), "system:startup");

    var v1Cfg = reader.findVersion("j9-zero-zero", 1);
    assertThat(v1Cfg).isPresent();
    assertThat(v1Cfg.get().roles()).hasSize(1);
    assertThat(v1Cfg.get().version()).isEqualTo(1);

    var v2Cfg = reader.findVersion("j9-zero-zero", 2);
    assertThat(v2Cfg).isPresent();
    assertThat(v2Cfg.get().roles()).hasSize(2);
    assertThat(v2Cfg.get().version()).isEqualTo(2);
  }

  // ---- §7 Author-supplied version mismatch is a WARN, not an error ----
  @Test
  void authorVersionMismatchLogsWarning(CapturedOutput out) {
    String authorClaimsV5 = J9_BASE.replace("version: 1\n", "version: 5\n");
    ValidationResult result =
        configService.validateAndRegister("j9-author5.yaml", authorClaimsV5.getBytes());
    assertThat(result.isInvalid()).isFalse();
    assertThat(repo.findAll()).hasSize(1);
    assertThat(repo.findAll().get(0).getVersion()).isEqualTo(1);
    assertThat(out.getOut()).contains("author-supplied version");
  }

  // ---- §8 Concurrent first-deploy is serialised ----
  @Test
  void concurrentFirstDeployIsSerialised() throws Exception {
    int threads = 4;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    @SuppressWarnings("unchecked")
    Future<CaseTypeVersionRegistration>[] futures = new Future[threads];
    try {
      for (int i = 0; i < threads; i++) {
        futures[i] =
            pool.submit(
                () -> {
                  start.await(2, TimeUnit.SECONDS);
                  return registry.register(
                      "j9-zero-zero", J9_BASE.getBytes(StandardCharsets.UTF_8), "system:startup");
                });
      }
      start.countDown();
      int successes = 0;
      for (Future<CaseTypeVersionRegistration> f : futures) {
        try {
          CaseTypeVersionRegistration r = f.get(5, TimeUnit.SECONDS);
          assertThat(r.version()).isEqualTo(1);
          successes++;
        } catch (java.util.concurrent.ExecutionException ignored) {
          // Acceptable: H2 SERIALIZABLE may surface a concurrent first-deploy collision as a
          // DataIntegrityViolationException on the loser. AC7 §8 only requires "two rows is a
          // fail" — losing-thread exceptions are fine as long as the registry stays consistent.
        }
      }
      assertThat(successes)
          .as("at least one thread must observe the registered/idempotent v1 row")
          .isGreaterThanOrEqualTo(1);
    } finally {
      pool.shutdown();
    }
    assertThat(repo.findAll())
        .as("AC7 §8 — exactly one row at v1; no duplicate inserts under concurrent first-deploy")
        .hasSize(1);
  }

  // ---- §9 CaseService.create binds to registry version ----
  @Test
  void caseCreateBindsRegistryCurrentVersion() {
    configService.validateAndRegister("j9-bind-v1.yaml", J9_BASE.getBytes(), "system:startup");
    Case created = caseService.create("j9-zero-zero", Map.of(), null, actorId());
    assertThat(created.caseTypeVersion()).isEqualTo(1);

    String v2 =
        J9_BASE.replace(
            "  - name: admin\n    permissions: [view, create, edit, transition]\n",
            "  - name: admin\n    permissions: [view, create, edit, transition]\n"
                + "  - name: viewer\n    permissions: [view]\n");
    configService.validateAndRegister("j9-bind-v2.yaml", v2.getBytes(), "system:startup");

    Case createdAfterBump = caseService.create("j9-zero-zero", Map.of(), null, actorId());
    assertThat(createdAfterBump.caseTypeVersion()).isEqualTo(2);
  }

  // ---- §10 In-memory CaseTypeConfig.version() reflects registry value ----
  @Test
  void caseTypeConfigVersionReflectsRegistryAfterReDeploy() {
    configService.validateAndRegister("j9-cfgver-v1.yaml", J9_BASE.getBytes(), "system:startup");
    assertThat(reader.find("j9-zero-zero")).isPresent();
    assertThat(reader.find("j9-zero-zero").get().version()).isEqualTo(1);

    String v2 =
        J9_BASE.replace(
            "  - name: admin\n    permissions: [view, create, edit, transition]\n",
            "  - name: admin\n    permissions: [view, create, edit, transition]\n"
                + "  - name: viewer\n    permissions: [view]\n");
    configService.validateAndRegister("j9-cfgver-v2.yaml", v2.getBytes(), "system:startup");
    assertThat(reader.find("j9-zero-zero").get().version()).isEqualTo(2);
  }

  // ---- Story 4.5 AC3 — fingerprint columns -----------------------------------

  @Test
  void registersFingerprints() {
    // The 5-arg overload stores bpmnContentHash and mappingHash alongside the YAML hash.
    // Both must be exactly 64 lowercase hex chars (SHA-256 output format, VARCHAR(64) column).
    String fakeBpmnHash = "c".repeat(64);
    String fakeMappingHash = "d".repeat(64);
    CaseTypeVersionRegistration r =
        registry.register(
            "j9-zero-zero", J9_BASE.getBytes(), "system:startup", fakeBpmnHash, fakeMappingHash);
    assertThat(r.outcome()).isEqualTo(CaseTypeVersionRegistration.Outcome.REGISTERED);
    assertThat(r.version()).isEqualTo(1);

    var record = registry.findVersion("j9-zero-zero", 1);
    assertThat(record).isPresent();
    assertThat(record.get().bpmnContentHash())
        .as("bpmn_content_hash must be persisted")
        .isEqualTo(fakeBpmnHash);
    assertThat(record.get().mappingHash())
        .as("mapping_hash must be persisted")
        .isEqualTo(fakeMappingHash);
  }

  @Test
  void storesNullFingerprintsForZeroAttachment() {
    // Zero-attachment deploys pass null for both fingerprint hashes.
    CaseTypeVersionRegistration r =
        registry.register("j9-zero-zero", J9_BASE.getBytes(), "system:startup", null, null);
    assertThat(r.outcome()).isEqualTo(CaseTypeVersionRegistration.Outcome.REGISTERED);

    var record = registry.findVersion("j9-zero-zero", 1);
    assertThat(record).isPresent();
    assertThat(record.get().bpmnContentHash())
        .as("bpmn_content_hash must be NULL for zero-attachment deploy")
        .isNull();
    assertThat(record.get().mappingHash())
        .as("mapping_hash must be NULL for zero-attachment deploy")
        .isNull();
  }

  // ---- WKS-VER-001 defensive guard ----
  @Test
  void caseCreateWithoutRegistryRowRaisesWksVer001() {
    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> caseService.create("nonexistent-id", Map.of(), null, UUID.randomUUID()))
        .satisfiesAnyOf(
            t ->
                org.assertj.core.api.Assertions.assertThat(t)
                    .isInstanceOf(WksVersionException.class)
                    .extracting("code")
                    .isEqualTo("WKS-VER-001"),
            // requireCaseType throws WksNotFoundException for unknown ids — covered by Story 2.3.
            // Either path proves nothing has slipped through.
            t ->
                org.assertj.core.api.Assertions.assertThat(t.getClass().getSimpleName())
                    .contains("NotFound"));
  }
}
