package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.service.ConfigService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
 * validates each, and registers the valid ones. Logs one WARN per validation error with structured
 * fields; emits a summary INFO line once the directory has been processed.
 *
 * <p>Failure policy: by default the application boots even if individual files fail validation —
 * valid configs still register. Set {@code wks.case-types.fail-on-invalid=true} to abort startup on
 * the first invalid file.
 */
@Component
public class CaseTypeStartupLoader {

  private static final Logger log = LoggerFactory.getLogger(CaseTypeStartupLoader.class);

  private final ConfigService configService;
  private final Path dir;
  private final boolean failOnInvalid;

  public CaseTypeStartupLoader(
      ConfigService configService,
      @Value("${wks.case-types.dir:./case-types/}") String dir,
      @Value("${wks.case-types.fail-on-invalid:false}") boolean failOnInvalid) {
    this.configService = configService;
    this.dir = Path.of(dir).toAbsolutePath();
    this.failOnInvalid = failOnInvalid;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void loadOnStartup() {
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
    List<String> failedFiles = new ArrayList<>();

    try (Stream<Path> files = Files.list(dir)) {
      List<Path> yamls =
          files
              .filter(Files::isRegularFile)
              .filter(CaseTypeStartupLoader::isYaml)
              .sorted()
              .toList();

      for (Path file : yamls) {
        ValidationResult result = configService.validateAndRegister(file);
        if (result.isInvalid()) {
          rejected++;
          failedFiles.add(file.getFileName().toString());
          result
              .errors()
              .forEach(
                  e ->
                      log.atWarn()
                          .addKeyValue("wksErrorCode", e.code())
                          .addKeyValue("file", file.getFileName().toString())
                          .addKeyValue("errorField", e.field())
                          .addKeyValue("line", e.line())
                          .log("case-type validation error: {}", e.message()));
        } else {
          registered++;
        }
      }
    } catch (IOException e) {
      log.atWarn()
          .addKeyValue("wksErrorCode", "WKS-CFG-099")
          .addKeyValue("dir", dir.toString())
          .log("case-types directory listing failed: {}", e.getMessage());
      skipped++;
    }

    log.atInfo()
        .addKeyValue("wks.config.startup.registered", registered)
        .addKeyValue("wks.config.startup.rejected", rejected)
        .addKeyValue("wks.config.startup.skipped", skipped)
        .log("wks.config.startup.summary");

    if (failOnInvalid && rejected > 0) {
      throw new CaseTypesStartupException(
          "Case-type startup failed in fail-on-invalid mode — "
              + rejected
              + " file(s) rejected: "
              + String.join(", ", failedFiles));
    }
  }

  private static boolean isYaml(Path p) {
    String name = p.getFileName().toString().toLowerCase();
    return name.endsWith(".yaml") || name.endsWith(".yml");
  }
}
