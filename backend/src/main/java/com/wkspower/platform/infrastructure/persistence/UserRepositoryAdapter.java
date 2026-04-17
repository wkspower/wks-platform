package com.wkspower.platform.infrastructure.persistence;

import com.wkspower.platform.domain.model.AuthenticationMaterial;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.infrastructure.persistence.entity.RoleEntity;
import com.wkspower.platform.infrastructure.persistence.entity.UserEntity;
import com.wkspower.platform.infrastructure.persistence.repository.RoleEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.UserEntityRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter for {@link UserRepository}. Maps between JPA entities and domain records. Email is
 * normalized to lowercase at every boundary so {@code Admin@X} and {@code admin@X} resolve to the
 * same row, aligning with the {@code users.email UNIQUE} constraint behaviour users expect.
 */
@Component
class UserRepositoryAdapter implements UserRepository {

  private final UserEntityRepository users;
  private final RoleEntityRepository roles;

  UserRepositoryAdapter(UserEntityRepository users, RoleEntityRepository roles) {
    this.users = users;
    this.roles = roles;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    String normalized = normalizeEmail(email);
    if (normalized == null) {
      return Optional.empty();
    }
    return users.findByEmail(normalized).map(UserRepositoryAdapter::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AuthenticationMaterial> findAuthMaterialByEmail(String email) {
    String normalized = normalizeEmail(email);
    if (normalized == null) {
      return Optional.empty();
    }
    return users.findByEmail(normalized).map(UserRepositoryAdapter::toAuthMaterial);
  }

  @Override
  @Transactional
  public User save(User user, String passwordHash) {
    String normalizedEmail = normalizeEmail(user.email());
    Set<RoleEntity> roleEntities =
        user.roles().stream()
            .map(
                name ->
                    roles
                        .findByName(name)
                        .orElseThrow(
                            () -> new IllegalStateException("Role not found in database: " + name)))
            .collect(Collectors.toCollection(HashSet::new));

    Instant now = Instant.now();
    UserEntity entity =
        users
            .findById(user.id())
            .map(
                existing -> {
                  existing.setEmail(normalizedEmail);
                  existing.setPasswordHash(passwordHash);
                  existing.setActive(user.active());
                  // Mutate the Hibernate-managed collection rather than replacing the reference —
                  // preserves the persistent-collection wrapper so orphan rows in user_roles are
                  // cleaned up correctly.
                  existing.getRoles().clear();
                  existing.getRoles().addAll(roleEntities);
                  existing.setUpdatedAt(now);
                  return existing;
                })
            .orElseGet(
                () ->
                    new UserEntity(
                        user.id(),
                        normalizedEmail,
                        passwordHash,
                        user.active(),
                        now,
                        now,
                        roleEntities));

    UserEntity saved = users.save(entity);
    return toDomain(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsWithRole(String roleName) {
    return users.existsByRoles_Name(roleName);
  }

  static User toDomain(UserEntity entity) {
    return new User(
        entity.getId(), entity.getEmail(), roleNames(entity.getRoles()), entity.isActive());
  }

  static AuthenticationMaterial toAuthMaterial(UserEntity entity) {
    return new AuthenticationMaterial(
        entity.getId(),
        entity.getEmail(),
        entity.getPasswordHash(),
        roleNames(entity.getRoles()),
        entity.isActive());
  }

  private static Set<String> roleNames(Set<RoleEntity> roleEntities) {
    return roleEntities.stream().map(RoleEntity::getName).collect(Collectors.toUnmodifiableSet());
  }

  private static String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    String stripped = email.strip();
    return stripped.isEmpty() ? null : stripped.toLowerCase(Locale.ROOT);
  }
}
