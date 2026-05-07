package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.wkspower.platform.WksPlatformApplication;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextException;

/**
 * Story 2.2 fail-fast guard for BPMN failures. Drops one good YAML+BPMN pair plus one YAML whose
 * BPMN trips archetype validation, then boots with {@code wks.case-types.fail-on-invalid=true} —
 * context refresh must abort with a {@link CaseTypesStartupException}.
 *
 * <p>Boots via {@link SpringApplication#run(Class, String...)} (not {@code @SpringBootTest}) to
 * avoid poisoning the context cache.
 */
class CaseTypeStartupLoaderBpmnFailFastIT {

  @Test
  void brokenBpmnAbortsContextWhenFailFast(@TempDir Path dir, @TempDir Path dbDir)
      throws IOException {
    Files.writeString(dir.resolve("good.yaml"), yaml("good", "good.bpmn"));
    Files.writeString(dir.resolve("good.bpmn"), validBpmn());
    Files.writeString(dir.resolve("broken.yaml"), yaml("broken", "broken.bpmn"));
    Files.writeString(dir.resolve("broken.bpmn"), brokenBpmn());

    String[] args = {
      "--spring.profiles.active=dev",
      "--server.port=0",
      "--wks.case-types.dir=" + dir,
      "--wks.case-types.fail-on-invalid=true",
      "--spring.datasource.url=jdbc:h2:file:" + dbDir.resolve("ff-it") + ";DB_CLOSE_DELAY=-1",
      "--camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive=false"
    };

    try {
      SpringApplication.run(WksPlatformApplication.class, args).close();
      fail("Expected fail-on-invalid to abort startup on broken BPMN");
    } catch (ApplicationContextException | IllegalStateException e) {
      Throwable cursor = e;
      boolean found = false;
      while (cursor != null) {
        if (cursor instanceof CaseTypesStartupException) {
          found = true;
          break;
        }
        cursor = cursor.getCause();
      }
      assertThat(found)
          .as("expected CaseTypesStartupException somewhere in the cause chain; got %s", e)
          .isTrue();
    }
  }

  private static String yaml(String id, String bpmnFile) {
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

  private static String validBpmn() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
        + " xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\""
        + " targetNamespace=\"http://wkspower.com/bpmn/test\">"
        + "<bpmn:process id=\"goodProcess\" isExecutable=\"true\" camunda:historyTimeToLive=\"30\">"
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
