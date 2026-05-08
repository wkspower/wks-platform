package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.BpmnValidationService;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.domain.workflow.BpmnValidationResult;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit-level coverage for {@link CaseTypeStartupLoader} that does not need a Spring context: drives
 * the loader directly with a stub {@link ConfigService} on a real temp directory.
 */
class CaseTypeStartupLoaderTest {

  @Test
  void invalidPathDoesNotThrow(@TempDir Path tmp) {
    assertThatNoException()
        .isThrownBy(
            () -> {
              CaseTypeStartupLoader loader =
                  new CaseTypeStartupLoader(stubService(), "bad path", false);
              loader.loadOnStartup();
            });
  }

  @Test
  void dotfilesAreSkipped(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve(".hidden.yaml"), "id: x");
    Files.writeString(dir.resolve("good.yaml"), "id: x");

    List<Path> seen = new ArrayList<>();
    ConfigService svc = recordingService(seen);
    CaseTypeStartupLoader loader = new CaseTypeStartupLoader(svc, dir.toString(), false);
    loader.loadOnStartup();

    assertThat(seen).extracting(p -> p.getFileName().toString()).containsExactly("good.yaml");
  }

  private static ConfigService stubService() {
    return buildService(p -> alwaysInvalid());
  }

  private static ConfigService recordingService(List<Path> seen) {
    return buildService(
        path -> {
          seen.add(path);
          return alwaysInvalid();
        });
  }

  private static ValidationResult alwaysInvalid() {
    return ValidationResult.invalid(List.of(ErrorDetail.of("WKS-CFG-099", "stub")));
  }

  private static ConfigService buildService(
      java.util.function.Function<Path, ValidationResult> loader) {
    return new ConfigService(
        new CaseTypeSource() {
          @Override
          public ValidationResult load(Path file) {
            return loader.apply(file);
          }

          @Override
          public ValidationResult loadBytes(
              String s, byte[] b, java.util.Map<String, byte[]> bpmnByName) {
            return alwaysInvalid();
          }
        },
        new CaseTypeRegistrar() {
          @Override
          public RegistrationResult register(CaseTypeConfig c) {
            return RegistrationResult.registered();
          }

          @Override
          public void remove(String id) {}
        },
        new CaseTypeReader() {
          @Override
          public Optional<CaseTypeConfig> find(String id) {
            return Optional.empty();
          }

          @Override
          public Collection<CaseTypeConfig> all() {
            return List.of();
          }

          @Override
          public Optional<CaseTypeConfig> findVersion(String id, int version) {
            return Optional.empty();
          }
        },
        (BpmnValidationService) (bytes, ct) -> BpmnValidationResult.ok("noop"),
        new WorkflowEngine() {
          @Override
          public DeploymentResult deploy(DeploymentRequest request) {
            return new DeploymentResult("d", request.processDefinitionKey(), "p", 1, Instant.now());
          }

          @Override
          public Optional<DeploymentInfo> latestDeployment(String key) {
            return Optional.empty();
          }

          @Override
          public String startProcessInstance(String key, java.util.Map<String, Object> variables) {
            return "pi-noop";
          }

          @Override
          public Optional<com.wkspower.platform.domain.model.Task> findTask(String taskId) {
            return Optional.empty();
          }

          @Override
          public void completeTask(String taskId, java.util.Map<String, Object> variables) {}

          @Override
          public void claimTask(String taskId, java.util.UUID userId) {}

          @Override
          public void signalTransition(
              String processInstanceId, String action, java.util.Map<String, Object> variables) {}

          @Override
          public java.util.List<com.wkspower.platform.domain.model.Task> findTasksByCase(
              java.util.UUID caseId) {
            return java.util.List.of();
          }

          @Override
          public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
            return null;
          }
        },
        event -> {},
        new com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry(),
        new com.wkspower.platform.domain.service.MappingRegistry());
  }
}
