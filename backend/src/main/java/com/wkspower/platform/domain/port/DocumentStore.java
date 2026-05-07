package com.wkspower.platform.domain.port;

import java.io.InputStream;
import java.util.UUID;

/**
 * Port for binary document storage (Story 14.2). Implementations: {@code LocalDocumentStore} (dev,
 * stores on the local filesystem) and {@code MinioDocumentStore} (production, stores in an
 * S3-compatible MinIO bucket). Selected at startup by {@code DocumentStoreConfig} based on whether
 * {@code wks.storage.endpoint} is blank.
 */
public interface DocumentStore {

  /**
   * Persist the given byte stream and return the implementation-specific storage key (a path
   * fragment for local store, an object key for MinIO).
   *
   * @param caseId the owning case UUID
   * @param documentId the document's own UUID
   * @param fileName sanitized filename (no directory separators)
   * @param stream the raw byte stream; caller is responsible for closing it after this call
   * @param contentType MIME type string
   * @param sizeBytes declared byte count (informational; implementations may re-measure)
   * @return the storage key to persist in {@code case_documents.storage_key}
   */
  String store(
      UUID caseId,
      UUID documentId,
      String fileName,
      InputStream stream,
      String contentType,
      long sizeBytes);

  /**
   * Retrieve the byte stream for the given storage key. The caller is responsible for closing the
   * returned stream.
   *
   * @param storageKey the value stored in {@code case_documents.storage_key}
   * @return a fresh {@link InputStream} positioned at the beginning of the stored bytes
   */
  InputStream retrieve(String storageKey);

  /** Remove the object identified by {@code storageKey} from the backing store. */
  void delete(String storageKey);

  /**
   * Returns a presigned GET URL for the object if the backing store supports it (MinIO), or {@code
   * null} if it does not (LocalDocumentStore). The default implementation returns {@code null} —
   * implementations that support presigned URLs override this method.
   */
  default String presignedUrl(String storageKey) {
    return null;
  }
}
