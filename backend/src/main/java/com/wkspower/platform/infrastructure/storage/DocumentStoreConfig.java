package com.wkspower.platform.infrastructure.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Selects the active {@link com.wkspower.platform.domain.port.DocumentStore} implementation (Story
 * 14.2 AC5).
 *
 * <ul>
 *   <li>If {@code wks.storage.endpoint} is blank → {@link LocalDocumentStore} (dev / eval).
 *   <li>Otherwise → {@link MinioDocumentStore} with a {@link MinioClient} wired from {@code
 *       wks.storage.endpoint} and env-var credentials {@code WKS_MINIO_ROOT_USER} / {@code
 *       WKS_MINIO_ROOT_PASSWORD}.
 * </ul>
 *
 * <p>On MinIO startup the config calls {@code bucketExists} + {@code makeBucket} to ensure the
 * target bucket exists (idempotent — no error if it already exists).
 */
@Configuration
public class DocumentStoreConfig {

  private static final Logger log = LoggerFactory.getLogger(DocumentStoreConfig.class);

  @Value("${wks.storage.endpoint:}")
  private String storageEndpoint;

  @Value("${wks.storage.key:}")
  private String storageKey;

  @Value("${wks.documents.local-store-path:./data/documents}")
  private String localStorePath;

  @Value("${wks.documents.bucket:wks-documents}")
  private String bucket;

  @Autowired private Environment env;

  @Bean
  public com.wkspower.platform.domain.port.DocumentStore documentStore() {
    if (storageEndpoint == null || storageEndpoint.isBlank()) {
      log.info("wks.storage.endpoint is blank — using LocalDocumentStore at '{}'", localStorePath);
      return new LocalDocumentStore(localStorePath);
    }

    log.info(
        "wks.storage.endpoint='{}' — using MinioDocumentStore (bucket='{}')",
        storageEndpoint,
        bucket);

    // P9: Hard-fail if default or blank MinIO credentials are in use.
    String minioUser = env.getProperty("WKS_MINIO_ROOT_USER", "");
    String minioPassword = env.getProperty("WKS_MINIO_ROOT_PASSWORD", "");
    if ("minioadmin".equals(minioUser)
        || "minioadmin".equals(minioPassword)
        || minioUser.isBlank()
        || minioPassword.isBlank()) {
      throw new IllegalStateException(
          "WKS_MINIO_ROOT_USER and WKS_MINIO_ROOT_PASSWORD must be set to non-default values"
              + " when MinIO storage is enabled (wks.storage.endpoint is non-blank)");
    }

    MinioClient minioClient =
        MinioClient.builder()
            .endpoint(storageEndpoint)
            .credentials(minioUser, minioPassword)
            .build();

    // Ensure the bucket exists (idempotent).
    try {
      boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
      if (!exists) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        log.info("Created MinIO bucket '{}'", bucket);
      } else {
        log.info("MinIO bucket '{}' already exists", bucket);
      }
    } catch (Exception e) {
      log.error("Failed to verify/create MinIO bucket '{}': {}", bucket, e.getMessage(), e);
      // Surface at runtime via WKS-DOC-005 — do not hard-fail startup (AC5 note: no liveness check
      // in ProductionBootstrapValidator per story AC5 constraint).
    }

    return new MinioDocumentStore(minioClient, bucket);
  }
}
