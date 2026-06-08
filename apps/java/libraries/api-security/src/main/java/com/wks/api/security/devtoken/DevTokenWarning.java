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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Loud startup warning emitted when the embedded dev-token issuer is active
 * ({@code wks.auth.mode=dev-token}). Mirrors the security-disabled warning
 * pattern. This mode mints self-signed JWTs and must never be used in
 * production.
 *
 * @author wks
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "wks.auth.mode", havingValue = "dev-token")
public class DevTokenWarning {

	@PostConstruct
	public void warn() {
		log.warn("==================================================================================");
		log.warn("  DEV-TOKEN AUTHENTICATION IS ACTIVE (wks.auth.mode=dev-token)");
		log.warn("  An embedded issuer is minting self-signed RS256 JWTs at /dev-auth/**.");
		log.warn("  This is for LOCAL DEVELOPMENT ONLY and MUST NEVER be used in production.");
		log.warn("==================================================================================");
	}

}
