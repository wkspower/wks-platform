package com.wkspower.platform.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.exception.WksDocumentException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalDocumentStoreTest {

  @Test
  void store_acceptsPlainFilename_whenBasePathContainsDotSegment(@TempDir Path tempDir)
      throws Exception {
    // Reproduces the dev-default config (`./data/documents`): a relative basePath
    // whose toAbsolutePath() form retains a `.` segment that normalize() would
    // otherwise remove. Before the fix, the unnormalized basePath failed the
    // startsWith() check against the normalized target, rejecting plain filenames
    // as path-traversal attempts.
    Path nested = tempDir.resolve("a").resolve(".").resolve("b");
    Files.createDirectories(nested);
    LocalDocumentStore store = new LocalDocumentStore(nested.toString());

    UUID caseId = UUID.randomUUID();
    UUID documentId = UUID.randomUUID();
    String key =
        store.store(
            caseId,
            documentId,
            "file.txt",
            new ByteArrayInputStream("payload".getBytes(StandardCharsets.UTF_8)),
            "text/plain",
            7);

    assertThat(key).isEqualTo(caseId + "/" + documentId + "/file.txt");
  }

  @Test
  void store_rejectsActualTraversalAttempt(@TempDir Path tempDir) {
    LocalDocumentStore store = new LocalDocumentStore(tempDir.toString());

    assertThatThrownBy(
            () ->
                store.store(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "../../etc/passwd",
                    new ByteArrayInputStream(new byte[0]),
                    "text/plain",
                    0))
        .isInstanceOf(WksDocumentException.class);
  }
}
