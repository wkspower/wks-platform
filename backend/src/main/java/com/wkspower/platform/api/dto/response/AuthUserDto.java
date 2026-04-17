package com.wkspower.platform.api.dto.response;

import java.util.Set;

/** Authenticated caller surface — returned by {@code /api/auth/login} and {@code /api/auth/me}. */
public record AuthUserDto(String id, String email, Set<String> roles) {}
