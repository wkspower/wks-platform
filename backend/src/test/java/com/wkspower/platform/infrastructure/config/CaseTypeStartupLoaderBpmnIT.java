package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.WorkflowEngine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Story 2.2 startup-loader BPMN coverage. Three case types under one temp dir:
 *
 * <ul>
 *   <li>good — valid YAML + valid BPMN → registers AND deploys.
 *   <li>missing-bpmn — valid YAML, BPMN file absent → registers, WKS-CFG-010 WARN.
 *   <li>broken-bpmn — valid YAML, BPMN missing the archetype → does NOT deploy, WKS-CFG-020 WARN.
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("dev")
@ContextConfiguration(initializers = CaseTypeStartupLoaderBpmnIT.LoaderAppenderInitializer.class)
class CaseTypeStartupLoaderBpmnIT {

  @TempDir static Path caseTypesDir;
  @TempDir static Path dbDir;

  /**
   * Holds the Logback appender attached to the {@link CaseTypeStartupLoader} logger via the {@link
   * LoaderAppenderInitializer}. Spring Boot resets the Logback context as part of context refresh,
   * so attaching from a {@code @BeforeEach} or static field initialiser is too late — Spring's
   * reset wipes the appender before {@link
   * org.springframework.boot.context.event.ApplicationReadyEvent} fires. The {@link
   * ApplicationContextInitializer} runs AFTER Spring Boot's Logback init but BEFORE the loader's
   * ready-event listener, which is the right window.
   */
  static ListAppender<ILoggingEvent> LOADER_LOGS;

  static class LoaderAppenderInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      Logger logger = (Logger) LoggerFactory.getLogger(CaseTypeStartupLoader.class);
      ListAppender<ILoggingEvent> appender = new ListAppender<>();
      appender.start();
      logger.addAppender(appender);
      LOADER_LOGS = appender;
    }
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) throws IOException {
    writeFixtures(caseTypesDir);
    reg.add("wks.case-types.dir", caseTypesDir::toString);
    reg.add("wks.case-types.fail-on-invalid", () -> "false");
    reg.add(
        "spring.datasource.url",
        () -> "jdbc:h2:file:" + dbDir.resolve("startup-bpmn-it") + ";DB_CLOSE_DELAY=-1");
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
  }

  @Autowired CaseTypeReader registry;
  @Autowired WorkflowEngine engine;

  @Test
  void allThreeYamlsRegister() {
    assertThat(registry.all())
        .as("all three valid YAMLs register; only the BPMN-clean pair deploys")
        .hasSize(3);
    assertThat(registry.find("good")).isPresent();
    assertThat(registry.find("missing-bpmn")).isPresent();
    assertThat(registry.find("broken-bpmn")).isPresent();
  }

  @Test
  void onlyTheCleanBpmnIsDeployed() {
    assertThat(engine.latestDeployment("goodProcess"))
        .as("good case type has its BPMN deployed to the engine")
        .isPresent();
    assertThat(engine.latestDeployment("brokenProcess"))
        .as("broken-bpmn case type fails archetype validation, never reaches the engine")
        .isEmpty();
  }

  @Test
  void summaryLogLineCarriesBpmnCounters() {
    // AC8.4 — the structured-log summary must include `bpmn_deployed` and `bpmn_rejected`.
    // Pre-context appender (above) captures the loader's ApplicationReadyEvent emission so we
    // can assert against the structured fields rather than scraping stdout.
    List<ILoggingEvent> events = LOADER_LOGS.list;
    ILoggingEvent summary =
        events.stream()
            .filter(e -> "wks.config.startup.summary".equals(e.getMessage()))
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError(
                        "AC8.4: expected a 'wks.config.startup.summary' log event from the "
                            + "startup loader; events seen: "
                            + events.stream().map(ILoggingEvent::getMessage).toList()));
    var kvs = summary.getKeyValuePairs();
    // Logback serialises addKeyValue values via String.valueOf, so the structured fields land as
    // strings ("1", "2") regardless of the source int.
    assertThat(kvs)
        .as("AC8.4: summary log must carry bpmn_deployed=1 (good case type)")
        .anyMatch(p -> "bpmn_deployed".equals(p.key) && "1".equals(String.valueOf(p.value)));
    assertThat(kvs)
        .as(
            "AC8.4: summary log must carry bpmn_rejected=2 (missing-bpmn declared-but-absent +"
                + " broken-bpmn validation failure)")
        .anyMatch(p -> "bpmn_rejected".equals(p.key) && "2".equals(String.valueOf(p.value)));
  }

  // ---- fixtures ----------------------------------------------------------

  private static void writeFixtures(Path dir) throws IOException {
    // 1. good — valid YAML + valid BPMN
    Files.writeString(dir.resolve("good.yaml"), validYaml("good", "good.bpmn"));
    Files.writeString(dir.resolve("good.bpmn"), validBpmn("goodProcess"));

    // 2. missing-bpmn — valid YAML, BPMN file absent
    Files.writeString(dir.resolve("missing-bpmn.yaml"), validYaml("missing-bpmn", "absent.bpmn"));

    // 3. broken-bpmn — valid YAML, BPMN with missing archetype on user task
    Files.writeString(dir.resolve("broken-bpmn.yaml"), validYaml("broken-bpmn", "broken.bpmn"));
    Files.writeString(dir.resolve("broken.bpmn"), brokenBpmn());
  }

  private static String validYaml(String id, String bpmnFile) {
    return "id: "
        + id
        + "\ndisplayName: "
        + id
        + "\nversion: 1\nworkflows:\n  bpmn: "
        + bpmnFile
        + "\nfields:\n"
        + "  - id: applicant_name\n"
        + "    displayName: Applicant\n"
        + "    type: text\n"
        + "    required: true\n"
        + "statuses:\n"
        + "  - id: open\n"
        + "    displayName: Open\n"
        + "listColumns: [applicant_name]\n"
        + "roles:\n"
        + "  - name: officer\n"
        + "    permissions: [view]\n";
  }

  private static String validBpmn(String key) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
        + " xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\""
        + " targetNamespace=\"http://wkspower.com/bpmn/test\">"
        + "<bpmn:process id=\""
        + key
        + "\" isExecutable=\"true\" camunda:historyTimeToLive=\"30\">"
        + "<bpmn:startEvent id=\"start\"/>"
        + "<bpmn:userTask id=\"draft\">"
        + "<bpmn:extensionElements>"
        + "<camunda:properties>"
        + "<camunda:property name=\"archetype\" value=\"submit_for_processing\"/>"
        + "</camunda:properties>"
        + "</bpmn:extensionElements>"
        + "</bpmn:userTask>"
        + "<bpmn:endEvent id=\"end\"/>"
        + "<bpmn:sequenceFlow id=\"f1\" sourceRef=\"start\" targetRef=\"draft\"/>"
        + "<bpmn:sequenceFlow id=\"f2\" sourceRef=\"draft\" targetRef=\"end\"/>"
        + "</bpmn:process>"
        + "</bpmn:definitions>";
  }

  private static String brokenBpmn() {
    // Missing camunda:properties → WKS-CFG-020 on the user task.
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
        + " xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\""
        + " targetNamespace=\"http://wkspower.com/bpmn/test\">"
        + "<bpmn:process id=\"brokenProcess\" isExecutable=\"true\""
        + " camunda:historyTimeToLive=\"30\">"
        + "<bpmn:startEvent id=\"start\"/>"
        + "<bpmn:userTask id=\"noArchetype\"/>"
        + "<bpmn:endEvent id=\"end\"/>"
        + "<bpmn:sequenceFlow id=\"f1\" sourceRef=\"start\" targetRef=\"noArchetype\"/>"
        + "<bpmn:sequenceFlow id=\"f2\" sourceRef=\"noArchetype\" targetRef=\"end\"/>"
        + "</bpmn:process>"
        + "</bpmn:definitions>";
  }
}
