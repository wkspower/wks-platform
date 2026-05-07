package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.CaseTypeVersionRegistration;
import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.EndEventMapping;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.domain.service.MappingRegistry;
import com.wkspower.platform.domain.service.WorkflowAdapterBinder;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeVersionJpaRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Story 4.5 AC4 — integration test for the BPMN attachment detach lifecycle.
 *
 * <p>Verifies the two core invariants of AC4:
 *
 * <ol>
 *   <li>After {@link WorkflowAdapterBinder#detach(CaseTypeRef)}, the prior version's mapping
 *       remains resolvable via {@link MappingRegistry} — in-flight cases frozen on their prior
 *       version can still resolve their mapping.
 *   <li>The detached adapter is no longer reachable via {@link WorkflowAdapterBinder#resolve}, so
 *       new routing calls for the detached case-type scope get the {@code NullAdapter}.
 * </ol>
 *
 * <p>This test wires the full Spring context (H2) so the fingerprint columns written by {@link
 * CaseTypeVersionRegistry#register(String, byte[], String, String, String)} are exercised on the
 * real JPA adapter + Flyway migration.
 */
@SpringBootTest
@ActiveProfiles("dev")
class BpmnAttachmentLifecycleIT {

  @TempDir static Path dbDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) {
    reg.add(
        "spring.datasource.url",
        () -> "jdbc:h2:file:" + dbDir.resolve("lifecycle-it") + ";DB_CLOSE_DELAY=-1");
    reg.add("wks.case-types.dir", () -> "");
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
  }

  private static final String CASE_TYPE_ID = "lifecycle-test-ct";
  private static final byte[] YAML_BYTES =
      ("id: "
              + CASE_TYPE_ID
              + "\n"
              + "displayName: \"Lifecycle Test\"\n"
              + "version: 1\n"
              + "roles:\n"
              + "  - name: admin\n"
              + "    permissions: [view, create, edit, transition]\n")
          .getBytes();

  @Autowired private CaseTypeVersionRegistry versionRegistry;
  @Autowired private CaseTypeVersionJpaRepository repo;
  @Autowired private MappingRegistry mappingRegistry;
  @Autowired private WorkflowAdapterBinder adapterBinder;
  @Autowired private ConfigService configService;

  @AfterEach
  void wipe() {
    repo.deleteAll();
  }

  /**
   * AC3 + AC4 — fingerprint persisted, detach leaves MappingRegistry intact for prior version.
   *
   * <p>Step-by-step:
   *
   * <ol>
   *   <li>Register a CaseType version with fingerprint hashes (simulating a BPMN-attached deploy).
   *   <li>Register a MappingDefinition in MappingRegistry for that (caseTypeId, version).
   *   <li>Detach the adapter for that CaseTypeRef via WorkflowAdapterBinder.
   *   <li>Assert MappingRegistry still resolves the prior version's mapping.
   *   <li>Assert the fingerprints are persisted in case_type_versions.
   * </ol>
   */
  @Test
  void detachLeavesMapRegistryResolvableForPriorVersion() {
    // Step 1 — Register version with fingerprints (AC3).
    // Both hashes must be exactly 64 lowercase hex chars (SHA-256 output format).
    String fakeBpmnHash = "a".repeat(64);
    String fakeMappingHash = "b".repeat(64);
    CaseTypeVersionRegistration reg =
        versionRegistry.register(
            CASE_TYPE_ID, YAML_BYTES, "test:lifecycle", fakeBpmnHash, fakeMappingHash);
    assertThat(reg.version()).isEqualTo(1);

    // Verify fingerprints are persisted (AC3)
    var record = versionRegistry.findVersion(CASE_TYPE_ID, 1);
    assertThat(record).isPresent();
    assertThat(record.get().bpmnContentHash())
        .as("bpmn_content_hash must be persisted")
        .isEqualTo(fakeBpmnHash);
    assertThat(record.get().mappingHash())
        .as("mapping_hash must be persisted")
        .isEqualTo(fakeMappingHash);

    // Step 2 — Register a MappingDefinition for (caseTypeId, "1")
    CaseTypeRef caseTypeRef = new CaseTypeRef(CASE_TYPE_ID, "1");
    MappingDefinition mapping =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "lifecycle.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.of(new EndEventMapping("draft -> approved")),
                    Map.of(),
                    List.of())));
    mappingRegistry.register(caseTypeRef, "1", mapping);

    // Verify mapping is registered before detach
    assertThat(mappingRegistry.resolve(caseTypeRef, "1"))
        .as("mapping must be resolvable before detach")
        .isPresent()
        .hasValueSatisfying(m -> assertThat(m.attachments()).hasSize(1));

    // Step 3 — Detach the adapter for that CaseTypeRef (AC4)
    // WorkflowAdapterBinder.detach removes the adapter's registration for the ref;
    // MappingRegistry is NOT touched.
    adapterBinder.detach(caseTypeRef);

    // Step 4 — Verify MappingRegistry still resolves the prior version (AC4 invariant)
    assertThat(mappingRegistry.resolve(caseTypeRef, "1"))
        .as("MappingRegistry must retain prior version mapping after detach (AC4)")
        .isPresent()
        .hasValueSatisfying(m -> assertThat(m.attachments()).hasSize(1));

    // Step 5 — Verify WorkflowAdapterBinder.resolve returns NullAdapter for the detached ref
    // (the adapter registration is removed on detach)
    var resolvedAdapter = adapterBinder.resolve(caseTypeRef);
    assertThat(resolvedAdapter.getClass().getSimpleName())
        .as("WorkflowAdapterBinder must return NullAdapter after detach")
        .isEqualTo("NullAdapter");
  }

  @Test
  void zeroAttachmentDeployStoresNullFingerprints() {
    // Zero-attachment deploys store NULL in fingerprint columns (AC3)
    CaseTypeVersionRegistration reg =
        versionRegistry.register(CASE_TYPE_ID, YAML_BYTES, "test:lifecycle", null, null);
    assertThat(reg.version()).isEqualTo(1);

    var record = versionRegistry.findVersion(CASE_TYPE_ID, 1);
    assertThat(record).isPresent();
    assertThat(record.get().bpmnContentHash())
        .as("bpmn_content_hash must be NULL for zero-attachment deploy")
        .isNull();
    assertThat(record.get().mappingHash())
        .as("mapping_hash must be NULL for zero-attachment deploy")
        .isNull();
  }

  /**
   * P6 — Story 4.5 AC3 E2E path: calls {@link ConfigService#deploy} with real BPMN bytes and
   * asserts the resulting {@code CaseTypeVersion} row carries a non-null 64-char SHA-256 hex {@code
   * bpmnContentHash}. This test exercises the real hash path through the service stack (not
   * fabricated hash strings directly) and validates that the BPMN fingerprint column is populated
   * by {@link com.wkspower.platform.infrastructure.config.CaseTypeContentHasher#hashBytes}.
   *
   * <p>Architecture note: {@code mappingHash} is null here because the {@link
   * com.wkspower.platform.domain.port.CaseTypeSource#loadBytes} path does not currently thread BPMN
   * bytes into the mapping validator (the YAML-only parse path and the BPMN bytes path are separate
   * phases in {@code ConfigService.deploy}). Mapping-hash coverage via {@code deploy} is deferred
   * to Story 4.6 when the multipart byte map is plumbed into {@code loadBytes}.
   *
   * <p>Uses a minimal valid BPMN (start → end event, no user tasks) so the BPMN validator passes
   * without archetype declarations.
   */
  @Test
  void deployViaConfigServicePersistsRealBpmnFingerprint() {
    // Minimal BPMN with one executable process and a start + end event — no user tasks so the
    // BPMN validator passes without archetype declarations.
    String processId = "lifecycle-hash-proc";
    byte[] bpmnBytes =
        ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n"
                + "                  xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\"\n"
                + "                  targetNamespace=\"http://wkspower.com/bpmn/test\"\n"
                + "                  id=\"Definitions_lifecycle_hash_proc\">\n"
                + "  <bpmn:process id=\""
                + processId
                + "\" isExecutable=\"true\""
                + " camunda:historyTimeToLive=\"30\">\n"
                + "    <bpmn:startEvent id=\"start\"/>\n"
                + "    <bpmn:endEvent id=\"end\"/>\n"
                + "    <bpmn:sequenceFlow id=\"f1\" sourceRef=\"start\" targetRef=\"end\"/>\n"
                + "  </bpmn:process>\n"
                + "</bpmn:definitions>\n")
            .getBytes(StandardCharsets.UTF_8);

    // YAML without attachments — uses workflow ref for engine deploy but no mapping validator
    // invocation, which is consistent with the current ConfigService.deploy separation between
    // the YAML-parse phase and the BPMN-bytes phase.
    String hashCaseTypeId = "lifecycle-hash-ct";
    byte[] yamlBytes =
        ("id: "
                + hashCaseTypeId
                + "\n"
                + "displayName: \"Hash Test\"\n"
                + "version: 1\n"
                + "workflows:\n"
                + "  bpmn: "
                + processId
                + ".bpmn\n"
                + "statuses:\n"
                + "  - id: open\n"
                + "    displayName: Open\n"
                + "roles:\n"
                + "  - name: admin\n"
                + "    permissions: [view, create, edit, transition]\n")
            .getBytes(StandardCharsets.UTF_8);

    DeployResult result = configService.deploy(yamlBytes, bpmnBytes, "test:p6");

    assertThat(result.isInvalid()).as("deploy must succeed; errors=" + result.errors()).isFalse();
    assertThat(result.caseType()).isPresent();
    int version = result.caseType().get().version();

    // Assert the version row has a real 64-char SHA-256 hex bpmnContentHash (AC3).
    var record = versionRegistry.findVersion(hashCaseTypeId, version);
    assertThat(record).as("version row must exist after deploy").isPresent();
    assertThat(record.get().bpmnContentHash())
        .as("bpmnContentHash must be a non-null 64-char SHA-256 hex (real hash, not fabricated)")
        .isNotNull()
        .hasSize(64)
        .matches("[0-9a-f]{64}");
    // mappingHash is null because YAML declares no attachments (zero-attachment deploy — D22).
    assertThat(record.get().mappingHash())
        .as("mappingHash is null for zero-attachment YAML (D22 first-class)")
        .isNull();
  }
}
