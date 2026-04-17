package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * JPA entity for {@code roles}. One-way relationship from {@code UserEntity → RoleEntity}; no
 * back-reference to avoid bidirectional mapping concerns.
 */
@Entity
@Table(name = "roles")
public class RoleEntity {

  @Id private UUID id;

  @Column(nullable = false, unique = true, length = 64)
  private String name;

  protected RoleEntity() {
    // JPA
  }

  public RoleEntity(UUID id, String name) {
    this.id = id;
    this.name = name;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
