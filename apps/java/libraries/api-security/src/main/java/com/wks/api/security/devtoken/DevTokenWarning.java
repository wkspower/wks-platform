/*
 * WKS Platform - Open-Source Project
 *
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 *
 * WKS Platform is licensed under the MIT License.
 *
 * © 2021 WKS Power. All rights reserved.
 *
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.api.security.devtoken;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Loud startup warning emitted when the embedded dev-token issuer is active
 * ({@code wks.auth.mode=dev-token}). Mirrors the security-disabled warning
 * pattern. This mode mints self-signed JWTs and must never be used in production.
 *
 * <p>As a guard it FAILS FAST (refuses to start) if dev-token mode is combined
 * with the {@code prod} Spring profile, unless explicitly overridden with
 * {@code wks.auth.devtoken.allow-in-prod=true} (testing only).
 *
 * @author wks
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "wks.auth.mode", havingValue = "dev-token")
public class DevTokenWarning {

	private final Environment environment;

	private final boolean allowInProd;

	public DevTokenWarning(Environment environment,
			@Value("${wks.auth.devtoken.allow-in-prod:false}") boolean allowInProd) {
		this.environment = environment;
		this.allowInProd = allowInProd;
	}

	@PostConstruct
	public void warnOrFailFast() {
		if (!allowInProd && environment.acceptsProfiles(Profiles.of("prod"))) {
			throw new IllegalStateException(
					"wks.auth.mode=dev-token must NEVER run under the 'prod' profile - refusing to start. "
							+ "Use a real identity provider (wks.auth.mode=keycloak), or set "
							+ "wks.auth.devtoken.allow-in-prod=true only for controlled testing.");
		}
		log.warn("==================================================================================");
		log.warn("  DEV-TOKEN AUTHENTICATION IS ACTIVE (wks.auth.mode=dev-token)");
		log.warn("  An embedded issuer is minting self-signed RS256 JWTs at /dev-auth/**.");
		log.warn("  This is for LOCAL DEVELOPMENT ONLY and MUST NEVER be used in production.");
		log.warn("==================================================================================");
	}

}
