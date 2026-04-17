package com.wkspower.platform.infrastructure.persistence.repository;

import com.wkspower.platform.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link UserEntity}. {@code @EntityGraph} on {@code findByEmail}
 * fetches {@code roles} eagerly so the adapter can map to the domain without triggering {@code
 * LazyInitializationException} (application runs with {@code open-in-view: false}).
 */
public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {

  @EntityGraph(attributePaths = "roles")
  Optional<UserEntity> findByEmail(String email);

  boolean existsByRoles_Name(String roleName);
}
