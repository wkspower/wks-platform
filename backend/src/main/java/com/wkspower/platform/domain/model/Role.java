package com.wkspower.platform.domain.model;

import java.util.UUID;

/**
 * A named role (e.g. {@code admin}). Roles are assigned to users via join table {@code user_roles}.
 */
public record Role(UUID id, String name) {}
