package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.WksPlatformApplication;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextException;
import org.springframework.test.context.ActiveProfiles;

/**
 * When {@code wks.case-types.fail-on-invalid=true} AND any YAML fails validation, context
 * refreshment must abort with a {@link CaseTypesStartupException}. We boot a fresh application
 * context manually rather than rely on {@code @SpringBootTest}'s context caching, which would
 * poison every subsequent test.
 */
@SpringBootTest
@ActiveProfiles("dev")
class CaseTypeStartupLoaderFailFastIT {

  @TempDir Path dir;

  @Test
  void failFastOnInvalidAbortsContext() throws IOException {
    Files.writeString(
        dir.resolve("red.yaml"),
        """
        id: loan-application
        displayName: Loan
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

    String[] args = {
      "--spring.profiles.active=dev",
      "--wks.case-types.dir=" + dir,
      "--wks.case-types.fail-on-invalid=true",
      "--spring.datasource.url=jdbc:h2:mem:failfast;DB_CLOSE_DELAY=-1",
      "--server.port=0"
    };

    try {
      SpringApplication.run(WksPlatformApplication.class, args).close();
      org.junit.jupiter.api.Assertions.fail("Expected fail-on-invalid to abort startup");
    } catch (ApplicationContextException | IllegalStateException e) {
      // Look for CaseTypesStartupException in the chain.
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
      assertThat(e).isInstanceOfAny(ApplicationContextException.class, IllegalStateException.class);
    }
  }
}
