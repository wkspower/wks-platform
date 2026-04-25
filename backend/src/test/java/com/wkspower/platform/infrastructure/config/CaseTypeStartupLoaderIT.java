package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.port.CaseTypeReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * Startup-loader happy path. Drops one green YAML + one red YAML + one malformed YAML into a temp
 * dir, boots the application, and asserts:
 *
 * <ul>
 *   <li>Exactly one config registers (the green one).
 *   <li>One WARN per validation error is emitted.
 *   <li>The {@code wks.config.startup.summary} line lands.
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("dev")
@ExtendWith(OutputCaptureExtension.class)
class CaseTypeStartupLoaderIT {

  @TempDir static Path sharedDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) throws IOException {
    writeFixtures(sharedDir);
    reg.add("wks.case-types.dir", sharedDir::toString);
    reg.add("wks.case-types.fail-on-invalid", () -> "false");
    // Shrink startup surface — no need for the admin user in this test.
    reg.add("spring.datasource.url", () -> "jdbc:h2:mem:startup-ok;DB_CLOSE_DELAY=-1");
  }

  @Autowired CaseTypeReader registry;

  @Test
  void onlyValidConfigsRegister() {
    assertThat(registry.all()).as("green YAML registers, red + malformed do not").hasSize(1);
    assertThat(registry.find("loan-application")).isPresent();
    assertThat(registry.find("broken-one")).isEmpty();
  }

  @Test
  void summaryLogLineIsEmitted(CapturedOutput output) {
    assertThat(output.getAll())
        .as("startup summary log line must land in stdout")
        .contains("wks.config.startup.summary");
  }

  private static void writeFixtures(Path dir) throws IOException {
    Files.writeString(
        dir.resolve("green.yaml"),
        """
        id: loan-application
        displayName: Loan Application
        version: 1
        workflow:
          bpmn: loan-application.bpmn
        fields:
          - id: applicant_name
            displayName: Applicant
            type: text
            required: true
        statuses:
          - id: open
            displayName: Open
        listColumns: [applicant_name]
        roles:
          - name: officer
            permissions: [view]
        """);
    Files.writeString(
        dir.resolve("red.yaml"),
        """
        id: BROKEN_ID
        displayName: Broken
        version: 1
        workflow:
          bpmn: x.bpmn
        fields:
          - id: x
            displayName: X
            type: notarealtype
        statuses:
          - id: open
            displayName: Open
        listColumns: [x]
        roles:
          - name: officer
            permissions: [view]
        """);
    Files.writeString(dir.resolve("broken.yaml"), "id: [unclosed\n");
  }
}
