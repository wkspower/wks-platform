package com.wkspower.platform.infrastructure.storage;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksDocumentException;
import com.wkspower.platform.domain.port.DocumentStore;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local-filesystem {@link DocumentStore} implementation (dev / eval). Stores files at {@code
 * {basePath}/{caseId}/{documentId}/{fileName}}. Selected by {@link DocumentStoreConfig} when {@code
 * wks.storage.endpoint} is blank.
 */
public class LocalDocumentStore implements DocumentStore {

  private static final Logger log = LoggerFactory.getLogger(LocalDocumentStore.class);

  private final Path basePath;

  public LocalDocumentStore(String basePath) {
    this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    log.info("LocalDocumentStore initialised at {}", this.basePath);
  }

  @Override
  public String store(
      UUID caseId,
      UUID documentId,
      String fileName,
      InputStream stream,
      String contentType,
      long sizeBytes) {
    Path dir = basePath.resolve(caseId.toString()).resolve(documentId.toString());
    try {
      Files.createDirectories(dir);
      // P12: boundary check — catches unicode/encoded traversal that survives the sanitizer.
      Path target = dir.resolve(fileName).normalize();
      if (!target.startsWith(basePath)) {
        throw new IllegalArgumentException("Path traversal detected in filename: " + fileName);
      }
      try (OutputStream out = Files.newOutputStream(target)) {
        stream.transferTo(out);
      }
      // Return a relative key so the store is portable across mount points.
      String key = caseId + "/" + documentId + "/" + fileName;
      log.debug("Stored document at local key={}", key);
      return key;
    } catch (Exception e) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_005,
          "Failed to store document on local filesystem: " + e.getMessage(),
          e);
    }
  }

  @Override
  public InputStream retrieve(String storageKey) {
    Path target = basePath.resolve(storageKey);
    try {
      if (!Files.exists(target)) {
        throw new WksDocumentException(
            ErrorCode.WKS_DOC_005, "Document file not found at storage key: " + storageKey);
      }
      return new FileInputStream(target.toFile());
    } catch (WksDocumentException e) {
      throw e;
    } catch (Exception e) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_005,
          "Failed to retrieve document from local filesystem: " + e.getMessage(),
          e);
    }
  }

  @Override
  public void delete(String storageKey) {
    Path target = basePath.resolve(storageKey);
    try {
      Files.deleteIfExists(target);
      log.debug("Deleted document at local key={}", storageKey);
    } catch (Exception e) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_005,
          "Failed to delete document from local filesystem: " + e.getMessage(),
          e);
    }
  }

  /** Returns the absolute path to a stored file (used for local preview streaming). */
  public Path resolvePath(String storageKey) {
    return basePath.resolve(storageKey);
  }
}
