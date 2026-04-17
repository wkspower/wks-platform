package com.wkspower.platform.security;

import java.util.Set;
import java.util.UUID;

/**
 * Lightweight user representation extracted from a validated JWT. Internal to {@code security/}.
 */
public record AuthenticatedUser(UUID id, String email, Set<String> roles) {}
