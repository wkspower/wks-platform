package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.service.ConfigService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Loads every {@code *.yaml} / {@code *.yml} file from {@code wks.case-types.dir} on startup,
 * validates each, and registers the valid ones. Story 2.2 extends the loop with BPMN deploy: when
 * the YAML's {@code workflow.bpmn} resolves to a real file under the same directory, the BPMN is
 * validated + deployed to the engine.
 *
 * <p>Failure policy: by default the application boots even if individual files fail validation. Set
 * {@code wks.case-types.fail-on-invalid=true} to abort startup on the first invalid file or BPMN
 * deploy failure.
 */
@Component
public class CaseTypeStartupLoader {

  private static final Logger log = LoggerFactory.getLogger(CaseTypeStartupLoader.class);

  /** Hard cap matches the admin endpoint and {@link CaseTypeYamlLoader#MAX_YAML_BYTES}. */
  private static final long MAX_BPMN_BYTES = 1_048_576L;

  private final ConfigService configService;
  private final Path dir;
  private final String dirSpec;
  private final boolean failOnInvalid;

  public CaseTypeStartupLoader(
      ConfigService configService,
      @Value("${wks.case-types.dir:./case-types/}") String dir,
      @Value("${wks.case-types.fail-on-invalid:false}") boolean failOnInvalid) {
    this.configService = configService;
    this.dirSpec = dir;
    Path resolved;
    try {
      resolved = Path.of(dir).toAbsolutePath();
    } catch (InvalidPathException e) {
      resolved = null;
    }
    this.dir = resolved;
    this.failOnInvalid = failOnInvalid;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void loadOnStartup() {
    if (dir == null) {
      log.atWarn()
          .addKeyValue("wksErrorCode", "-")
          .addKeyValue("dir", dirSpec)
          .log("invalid case-types dir path; skipping");
      return;
    }
    if (!Files.isDirectory(dir)) {
      log.atInfo()
          .addKeyValue("wksErrorCode", "-")
          .addKeyValue("dir", dir.toString())
          .log("no case-types dir; skipping");
      return;
    }

    int registered = 0;
    int rejected = 0;
    int skipped = 0;
    int bpmnDeployed = 0;
    int bpmnRejected = 0;
    List<String> failedFiles = new ArrayList<>();
    List<String> bpmnFailedFiles = new ArrayList<>();

    try (Stream<Path> files = Files.list(dir)) {
      List<Path> yamls =
          files
              .filter(Files::isRegularFile)
              .filter(CaseTypeStartupLoader::isYaml)
              .sorted(Comparator.comparing(Path::toString))
              .toList();

      for (Path file : yamls) {
        FileOutcome outcome = processFile(file);
        registered += outcome.registered();
        rejected += outcome.rejected();
        bpmnDeployed += outcome.bpmnDeployed();
        bpmnRejected += outcome.bpmnRejected();
        if (outcome.rejected() > 0) {
          failedFiles.add(file.getFileName().toString());
        }
        if (outcome.bpmnRejected() > 0) {
          bpmnFailedFiles.add(file.getFileName().toString());
        }
      }
    } catch (IOException e) {
      log.atWarn()
          .addKeyValue("wksErrorCode", "WKS-CFG-099")
          .addKeyValue("dir", dir.toString())
          .log("case-types directory listing failed: {}", e.getMessage());
      skipped++;
      if (failOnInvalid) {
        throw new CaseTypesStartupException(
            "Case-type startup failed in fail-on-invalid mode — directory listing failed: "
                + e.getMessage());
      }
    }

    log.atInfo()
        .addKeyValue("registered", registered)
        .addKeyValue("rejected", rejected)
        .addKeyValue("skipped", skipped)
        .addKeyValue("bpmn_deployed", bpmnDeployed)
        .addKeyValue("bpmn_rejected", bpmnRejected)
        .log("wks.config.startup.summary");

    if (failOnInvalid && (rejected > 0 || bpmnRejected > 0)) {
      List<String> all = new ArrayList<>(failedFiles);
      all.addAll(bpmnFailedFiles);
      throw new CaseTypesStartupException(
          "Case-type startup failed in fail-on-invalid mode — "
              + (rejected + bpmnRejected)
              + " file(s) rejected: "
              + String.join(", ", all));
    }
  }

  private FileOutcome processFile(Path file) {
    byte[] yamlBytes;
    try {
      yamlBytes = Files.readAllBytes(file);
    } catch (IOException e) {
      log.atWarn()
          .addKeyValue("wksErrorCode", "WKS-CFG-099")
          .addKeyValue("file", file.getFileName().toString())
          .log("case-type read failed: {}", e.getMessage());
      return FileOutcome.yamlRejected();
    }

    // Resolve BPMN path purely from raw YAML — peek for `workflow.bpmn` without committing to
    // a parser dependency. If the YAML is invalid, the validator surfaces the right error;
    // resolveBpmnFilename returns null and we fall back to the YAML-only path.
    String bpmnFilename = peekBpmnFilename(yamlBytes);
    Path bpmnPath = resolveBpmnInsideCaseTypesDir(file, bpmnFilename);
    if (bpmnFilename != null && bpmnPath == null) {
      // Filename declared but rejected as outside the case-types dir or otherwise unsafe.
      log.atWarn()
          .addKeyValue("wksErrorCode", ErrorCode.WKS_CFG_010.wire())
          .addKeyValue("file", file.getFileName().toString())
          .addKeyValue("bpmn", bpmnFilename)
          .log(
              "BPMN filename rejected — must resolve to a regular file inside the case-types dir"
                  + " (no '..', no absolute paths, no symlink-cross-boundary)");
      return new FileOutcome(0, 1, 0, 1);
    }
    boolean bpmnPresent = bpmnPath != null && Files.isRegularFile(bpmnPath);

    if (!bpmnPresent) {
      // Two sub-cases:
      //  (a) bpmnFilename == null  → YAML legitimately has no `workflow:` block. This is a
      //      YAML-only deploy, NOT a failure — counters must not flag bpmnRejected.
      //  (b) bpmnFilename != null  → BPMN was declared but the file is missing. WKS-CFG-010 WARN,
      //      YAML still registers, bpmnRejected counted (fail-on-invalid trips).
      ValidationResult result = configService.validateAndRegister(file);
      if (result.isInvalid()) {
        logErrors(result.errors(), file);
        return FileOutcome.yamlRejected();
      }
      if (bpmnFilename == null) {
        // YAML-only — no BPMN declared, nothing to deploy, nothing to reject.
        return new FileOutcome(1, 0, 0, 0);
      }
      log.atWarn()
          .addKeyValue("wksErrorCode", ErrorCode.WKS_CFG_010.wire())
          .addKeyValue("file", file.getFileName().toString())
          .addKeyValue("bpmn", bpmnFilename)
          .log("BPMN file missing — case-type registered without engine deploy");
      return new FileOutcome(1, 0, 0, 1);
    }

    byte[] bpmnBytes;
    try {
      if (Files.size(bpmnPath) > MAX_BPMN_BYTES) {
        // D6: register YAML, mark BPMN rejected (fail-on-invalid mode trips on bpmnRejected>0,
        // matching the asymmetry already used for other BPMN errors). Don't fail the whole YAML.
        ValidationResult yamlOnly = configService.validateAndRegister(file);
        if (yamlOnly.isInvalid()) {
          logErrors(yamlOnly.errors(), file);
          return FileOutcome.yamlRejected();
        }
        log.atWarn()
            .addKeyValue("wksErrorCode", ErrorCode.WKS_CFG_010.wire())
            .addKeyValue("file", file.getFileName().toString())
            .addKeyValue("bpmn", bpmnFilename)
            .log("BPMN file exceeds 1 MB cap — case-type registered without engine deploy");
        return new FileOutcome(1, 0, 0, 1);
      }
      bpmnBytes = Files.readAllBytes(bpmnPath);
    } catch (IOException e) {
      log.atWarn()
          .addKeyValue("wksErrorCode", ErrorCode.WKS_CFG_010.wire())
          .addKeyValue("file", file.getFileName().toString())
          .addKeyValue("bpmn", bpmnFilename)
          .log("BPMN read failed: {}", e.getMessage());
      return new FileOutcome(0, 1, 0, 1);
    }

    // AC8.2: register YAML first (so a BPMN failure does NOT lose the case-type config), then
    // run BPMN validate + engine deploy as a separate side path. The HTTP path is atomic (deploy
    // rolls back the registry on engine failure) but startup intentionally tolerates BPMN
    // failures so the operator gets a partial-but-readable config in the registry.
    ValidationResult yamlResult = configService.validateAndRegister(file);
    if (yamlResult.isInvalid()) {
      logErrors(yamlResult.errors(), file);
      return FileOutcome.yamlRejected();
    }
    if (yamlResult.hasWarnings()) {
      logWarnings(yamlResult.warnings(), file);
    }
    CaseTypeConfig caseType = yamlResult.config().orElseThrow();

    DeployResult bpmnResult;
    try {
      bpmnResult = configService.deployBpmnFor(caseType, bpmnBytes, null);
    } catch (WksWorkflowEngineException ex) {
      log.atError()
          .addKeyValue("wksErrorCode", ex.getCode())
          .addKeyValue("file", file.getFileName().toString())
          .setCause(ex)
          .log("BPMN engine deploy failed");
      return new FileOutcome(1, 0, 0, 1);
    }

    if (bpmnResult.isInvalid()) {
      logErrors(bpmnResult.errors(), file);
      return new FileOutcome(1, 0, 0, 1);
    }

    return new FileOutcome(1, 0, 1, 0);
  }

  private static void logErrors(List<ErrorDetail> errors, Path file) {
    for (ErrorDetail e : errors) {
      log.atWarn()
          .addKeyValue("wksErrorCode", e.code())
          .addKeyValue("file", file.getFileName().toString())
          .addKeyValue("errorField", e.field())
          .addKeyValue("line", e.line())
          .log("case-type validation error: {}", e.message());
    }
  }

  /** Story 2.7 — log validator warnings (non-blocking findings, e.g. WKS-CFG-013) at WARN. */
  private static void logWarnings(List<ErrorDetail> warnings, Path file) {
    for (ErrorDetail w : warnings) {
      log.atWarn()
          .addKeyValue("wksErrorCode", w.code())
          .addKeyValue("file", file.getFileName().toString())
          .addKeyValue("warningField", w.field())
          .addKeyValue("line", w.line())
          .log("case-type validation warning: {}", w.message());
    }
  }

  /**
   * Resolve {@code bpmnFilename} against the YAML's parent directory and verify the result stays
   * inside the configured case-types dir. Returns {@code null} when the filename is null, when it
   * contains traversal segments, when it is absolute, or when {@code realpath} falls outside the
   * case-types dir (e.g. via symlink). The case-types dir is privileged input but operators may
   * still drop YAMLs from untrusted sources; defense-in-depth costs little here.
   */
  Path resolveBpmnInsideCaseTypesDir(Path yamlFile, String bpmnFilename) {
    if (bpmnFilename == null || bpmnFilename.isBlank()) {
      return null;
    }
    Path candidate;
    try {
      candidate = Path.of(bpmnFilename);
    } catch (InvalidPathException e) {
      return null;
    }
    if (candidate.isAbsolute()) {
      return null;
    }
    for (Path segment : candidate) {
      if ("..".equals(segment.toString())) {
        return null;
      }
    }
    Path resolved = yamlFile.getParent().resolve(candidate);
    // Compare via absolute-normalised paths so the boundary check works whether or not the BPMN
    // file exists yet (a missing file should still pass the policy check, then be flagged as
    // "BPMN missing" downstream — not blocked here as a security violation). When both paths
    // exist, additionally re-check via toRealPath to catch symlink-escape attempts.
    Path baseAbs = dir.toAbsolutePath().normalize();
    Path resolvedAbs = resolved.toAbsolutePath().normalize();
    if (!resolvedAbs.startsWith(baseAbs)) {
      return null;
    }
    if (Files.exists(resolved)) {
      try {
        Path baseReal = dir.toRealPath();
        Path resolvedReal = resolved.toRealPath();
        if (!resolvedReal.startsWith(baseReal)) {
          return null;
        }
      } catch (IOException e) {
        return null;
      }
    }
    return resolved;
  }

  /** Single shared mapper — YAMLMapper is thread-safe after construction. */
  private static final YAMLMapper PEEK_MAPPER = new YAMLMapper();

  /**
   * Peek for {@code workflow.bpmn} via the same YAML parser the validator uses. A real parse
   * handles inline comments, multi-document YAML, flow style, and quoting consistently with the
   * downstream loader — eliminating the divergence between the peek and the validator that the
   * hand-rolled scanner used to allow. Returns {@code null} if the YAML is unparseable or has no
   * such key.
   */
  static String peekBpmnFilename(byte[] yamlBytes) {
    JsonNode root;
    try {
      root = PEEK_MAPPER.readTree(yamlBytes);
    } catch (IOException e) {
      // Malformed YAML — the validator will surface the real error; from here we know nothing
      // about the BPMN, so fall through to the missing-BPMN path.
      return null;
    }
    if (root == null || !root.isObject()) {
      return null;
    }
    JsonNode workflow = root.get("workflow");
    if (workflow == null || !workflow.isObject()) {
      return null;
    }
    JsonNode bpmn = workflow.get("bpmn");
    if (bpmn == null || !bpmn.isTextual()) {
      return null;
    }
    String value = bpmn.asText();
    return value.isBlank() ? null : value;
  }

  private static boolean isYaml(Path p) {
    String fileName = p.getFileName().toString();
    if (fileName.startsWith(".")) {
      return false;
    }
    String name = fileName.toLowerCase();
    return name.endsWith(".yaml") || name.endsWith(".yml");
  }

  private record FileOutcome(int registered, int rejected, int bpmnDeployed, int bpmnRejected) {
    static FileOutcome yamlRejected() {
      return new FileOutcome(0, 1, 0, 0);
    }
  }
}
