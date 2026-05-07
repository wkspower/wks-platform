package com.wkspower.platform.domain.service;

import java.time.Instant;

/**
 * Immutable point-in-time view of the resolved license state. Returned by {@link
 * LicenseService#getLicenseSnapshot()} so callers can read all fields from a single consistent read
 * without risk of torn reads across multiple service calls.
 */
public record LicenseSnapshot(LicenseState licenseState, String tier, Instant expiry) {}
