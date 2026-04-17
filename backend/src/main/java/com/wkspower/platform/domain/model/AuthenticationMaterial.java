package com.wkspower.platform.domain.model;

import java.util.Set;
import java.util.UUID;

/**
 * Credentials material used only by {@code security/} to authenticate a login attempt. Carries the
 * Argon2id {@code passwordHash} — never expose this record to {@code api/} or log it.
 */
public record AuthenticationMaterial(
    UUID id, String email, String passwordHash, Set<String> roles, boolean active) {}
