package com.wkspower.platform.infrastructure.storage;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksDocumentException;
import com.wkspower.platform.domain.port.DocumentStore;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MinIO {@link DocumentStore} implementation (production). Stores objects in the configured bucket
 * with key {@code {caseId}/{documentId}/{fileName}}. Selected by {@link DocumentStoreConfig} when
 * {@code wks.storage.endpoint} is non-blank.
 *
 * <p>Presigned GET URLs are valid for 5 minutes (AC3 contract). The bucket is created on startup if
 * it does not already exist.
 */
public class MinioDocumentStore implements DocumentStore {

  private static final Logger log = LoggerFactory.getLogger(MinioDocumentStore.class);
  // P7: reduced from 5 minutes to 60 seconds to limit RBAC bypass window.
  private static final int PRESIGNED_URL_EXPIRY_SECONDS = 60;

  private final MinioClient minioClient;
  private final String bucket;

  public MinioDocumentStore(MinioClient minioClient, String bucket) {
    this.minioClient = minioClient;
    this.bucket = bucket;
  }

  @Override
  public String store(
      UUID caseId,
      UUID documentId,
      String fileName,
      InputStream stream,
      String contentType,
      long sizeBytes) {
    String objectKey = caseId + "/" + documentId + "/" + fileName;
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, sizeBytes, -1)
              .contentType(contentType)
              .build());
      log.debug("Stored document in MinIO bucket={} key={}", bucket, objectKey);
      return objectKey;
    } catch (Exception e) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_005, "Failed to store document in MinIO: " + e.getMessage(), e);
    }
  }

  @Override
  public InputStream retrieve(String storageKey) {
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(bucket).object(storageKey).build());
    } catch (Exception e) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_005, "Failed to retrieve document from MinIO: " + e.getMessage(), e);
    }
  }

  @Override
  public void delete(String storageKey) {
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(bucket).object(storageKey).build());
      log.debug("Deleted document from MinIO bucket={} key={}", bucket, storageKey);
    } catch (Exception e) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_005, "Failed to delete document from MinIO: " + e.getMessage(), e);
    }
  }

  /**
   * Returns a presigned GET URL valid for {@value #PRESIGNED_URL_EXPIRY_SECONDS} seconds. Used by
   * the preview endpoint (AC3). P7: TTL reduced from 5 minutes to 60 seconds to shrink the window
   * during which the URL can be used without platform RBAC. The URL grants unauthenticated access
   * for the TTL duration and must not be cached or forwarded by the caller.
   *
   * <p>Overrides the default no-op on {@link com.wkspower.platform.domain.port.DocumentStore}.
   */
  @Override
  public String presignedUrl(String storageKey) {
    try {
      return minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(bucket)
              .object(storageKey)
              .expiry(PRESIGNED_URL_EXPIRY_SECONDS, TimeUnit.SECONDS)
              .build());
    } catch (Exception e) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_005, "Failed to generate presigned URL: " + e.getMessage(), e);
    }
  }
}
