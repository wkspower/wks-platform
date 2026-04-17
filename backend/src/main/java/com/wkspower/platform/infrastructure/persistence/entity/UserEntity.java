package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity for {@code users}. The ID is supplied by the adapter (the domain {@code User} holds
 * the UUID), so no {@code @GeneratedValue} annotation. {@code @Version} provides optimistic locking
 * to catch concurrent updates cleanly.
 */
@Entity
@Table(name = "users")
public class UserEntity {

  @Id private UUID id;

  @Column(nullable = false, unique = true, length = 320)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 512)
  private String passwordHash;

  @Column(nullable = false)
  private boolean active;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

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
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.active = active;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.roles = roles;
  }

  public UUID getId() {
    return id;
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
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

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setRoles(Set<RoleEntity> roles) {
    this.roles = roles;
  }
}
