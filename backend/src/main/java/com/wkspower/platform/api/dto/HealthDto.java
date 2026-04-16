package com.wkspower.platform.api.dto;

/**
 * Health probe payload.
 *
 * @param version semver string from {@code BuildProperties} (build-info.properties)
 * @param uptime ISO-8601 duration from JVM {@code RuntimeMXBean}
 */
public record HealthDto(String version, String uptime) {}
