package com.wkspower.platform.domain.model;

import java.util.Set;
import java.util.UUID;

/**
 * Authenticated user as seen by the domain. Note the absence of a {@code passwordHash} field —
 * hashes live only in {@link AuthenticationMaterial} and are surfaced exclusively through {@code
 * UserRepository.findAuthMaterialByEmail} for use by {@code security/}. This prevents the hash from
 * accidentally appearing in controller responses or logs.
 */
public record User(UUID id, String email, Set<String> roles, boolean active) {

  public User {
    roles = roles == null ? Set.of() : Set.copyOf(roles);
  }
}
