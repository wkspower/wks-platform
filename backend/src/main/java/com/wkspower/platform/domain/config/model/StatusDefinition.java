package com.wkspower.platform.domain.config.model;

/** One custom status declared by a case type. {@code color} is optional. */
public record StatusDefinition(String id, String displayName, StatusColor color) {}
