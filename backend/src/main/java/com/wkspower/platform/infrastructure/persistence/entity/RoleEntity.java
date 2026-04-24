package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for {@code roles}. One-way relationship from {@code UserEntity → RoleEntity}; no
 * back-reference to avoid bidirectional mapping concerns.
 */
@Entity
@Table(name = "roles")
public class RoleEntity extends BaseJpaEntity {

  @Column(nullable = false, unique = true, length = 64)
  private String name;

  protected RoleEntity() {
    // JPA
  }

  public RoleEntity(UUID id, String name) {
    super(id);
    this.name = name;
  }

  public RoleEntity(UUID id, String name, Instant createdAt, Instant updatedAt) {
    super(id);
    this.name = name;
    setCreatedAt(createdAt);
    setUpdatedAt(updatedAt);
  }

  public String getName() {
    return name;
  }
}
