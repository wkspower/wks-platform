package com.wkspower.platform.infrastructure.persistence.entity;

import com.wkspower.platform.domain.port.DocumentStore;
import jakarta.persistence.PreRemove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA entity listener for {@link CaseDocumentEntity} (Story 14.2 — P5 review patch).
 *
 * <p>Deletes the backing storage object before the JPA row is removed. This covers cascade-delete
 * via {@code ON DELETE CASCADE} on {@code case_id}: when a case is removed, the DB cascade fires
 * {@code DELETE} on {@code case_documents} rows, which triggers {@link #beforeRemove}, ensuring the
 * corresponding storage objects (local filesystem or MinIO) are cleaned up and no orphaned files
 * are left behind.
 *
 * <p>Spring injects the {@link DocumentStore} bean via {@code @Autowired} on the listener
 * component. The {@code @Component} annotation registers this class in the Spring context so that
 * JPA entity listener injection works with Spring Data JPA's {@code SharedEntityManagerCreator}.
 */
@Component
public class CaseDocumentEntityListener {

  private static final Logger log = LoggerFactory.getLogger(CaseDocumentEntityListener.class);

  // Spring injects the DocumentStore bean into the static field so it is available
  // from the JPA callback which does not go through the Spring proxy chain.
  private static DocumentStore documentStore;

  @Autowired
  public void setDocumentStore(DocumentStore ds) {
    CaseDocumentEntityListener.documentStore = ds;
  }

  /**
   * Called by JPA before {@code case_documents} row is deleted. Removes the backing storage object
   * to prevent orphaned files when a case is cascade-deleted.
   */
  @PreRemove
  public void beforeRemove(CaseDocumentEntity entity) {
    if (documentStore == null) {
      log.warn(
          "DocumentStore not injected into CaseDocumentEntityListener — "
              + "storage object will not be cleaned up for document {}",
          entity.getId());
      return;
    }
    String key = entity.getStorageKey();
    try {
      documentStore.delete(key);
      log.debug("Storage object deleted on cascade remove: key={}", key);
    } catch (Exception e) {
      // Log but do not rethrow — allow the JPA delete to proceed. A leaked storage object
      // is preferable to blocking a cascade delete and leaving the DB in an inconsistent state.
      log.warn(
          "Failed to delete storage object on cascade remove: key={} error={}",
          key,
          e.getMessage(),
          e);
    }
  }
}
