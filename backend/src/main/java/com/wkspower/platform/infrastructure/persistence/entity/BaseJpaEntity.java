package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

/**
 * Shared mapping superclass for every JPA entity in the platform. Owns the four audit fields every
 * table gets — {@code id UUID PK}, {@code version BIGINT} (optimistic locking), {@code created_at},
 * {@code updated_at} — plus the {@link PrePersist} / {@link PreUpdate} callbacks that stamp the
 * timestamps automatically so adapters don't need to remember.
 *
 * <p>Lives in {@code infrastructure.persistence.entity} by ArchUnit rule — domain never sees this
 * type. Adapters map {@code BaseJpaEntity}-rooted JPA entities to pure-Java domain records at the
 * boundary.
 *
 * <p>The ID is not {@code @GeneratedValue}: callers supply a UUID (generated in the domain or
 * adapter). This keeps IDs deterministic for tests and preserves the AC6 rule that every identifier
 * on the wire is a UUID.
 */
@MappedSuperclass
public abstract class BaseJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected BaseJpaEntity() {
    // JPA
  }

  protected BaseJpaEntity(UUID id) {
    this.id = id;
  }

  @PrePersist
  void onPrePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
  }

  @PreUpdate
  void onPreUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  protected void setId(UUID id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /** Exposed so adapters can force a specific timestamp (e.g. to preserve audit values). */
  protected void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  protected void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
