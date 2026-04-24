package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity for {@code users}. ID, version, and audit timestamps live on {@link BaseJpaEntity} —
 * this class owns only the user-specific columns.
 */
@Entity
@Table(name = "users")
public class UserEntity extends BaseJpaEntity {

  @Column(nullable = false, unique = true, length = 320)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 512)
  private String passwordHash;

  @Column(nullable = false)
  private boolean active;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<RoleEntity> roles = new HashSet<>();

  protected UserEntity() {
    // JPA
  }

  public UserEntity(
      UUID id,
      String email,
      String passwordHash,
      boolean active,
      Instant createdAt,
      Instant updatedAt,
      Set<RoleEntity> roles) {
    super(id);
    this.email = email;
    this.passwordHash = passwordHash;
    this.active = active;
    setCreatedAt(createdAt);
    setUpdatedAt(updatedAt);
    this.roles = roles;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public boolean isActive() {
    return active;
  }

  public Set<RoleEntity> getRoles() {
    return roles;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public void setUpdatedAt(Instant updatedAt) {
    super.setUpdatedAt(updatedAt);
  }

  public void setRoles(Set<RoleEntity> roles) {
    this.roles = roles;
  }
}
