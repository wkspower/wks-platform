package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.model.AuthenticationMaterial;
import com.wkspower.platform.domain.model.User;
import java.util.Optional;

/**
 * Domain port for user persistence. The adapter ({@code infrastructure/persistence}) implements
 * this interface; application code depends only on the port.
 *
 * <p><b>Hash isolation:</b> {@link #findAuthMaterialByEmail(String)} is the <i>only</i> method that
 * returns the password hash and must be called exclusively from {@code security/}. Other code paths
 * must use {@link #findByEmail(String)} which returns a {@link User} without the hash.
 */
public interface UserRepository {

  /**
   * Returns the domain {@link User} (without password hash) if a user with the given email exists.
   */
  Optional<User> findByEmail(String email);

  /**
   * Returns credential material for authentication. Only {@code security/} should call this method.
   * Never put the returned record in logs, responses, or exception messages.
   */
  Optional<AuthenticationMaterial> findAuthMaterialByEmail(String email);

  /**
   * Creates (or updates) a user with the supplied Argon2id password hash. The hash is supplied
   * separately because the domain {@link User} does not carry it.
   */
  User save(User user, String passwordHash);

  /** Returns {@code true} if any user currently has the given role assigned. */
  boolean existsWithRole(String roleName);
}
