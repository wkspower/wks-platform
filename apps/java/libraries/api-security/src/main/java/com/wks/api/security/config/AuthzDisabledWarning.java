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
package com.wks.api.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Logs a loud warning at startup when {@code wks.authz.opa.enabled=false} so a
 * deployment running with OPA authorization disabled (authentication stays on,
 * every authenticated request is permitted) is impossible to miss in container
 * logs. Intended for minimal/dev/single-tenant setups only.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "wks.authz.opa.enabled", havingValue = "false")
public class AuthzDisabledWarning {

	@PostConstruct
	public void warn() {
		log.warn("******************************************************************");
		log.warn("*  WKS OPA AUTHORIZATION IS DISABLED (wks.authz.opa.enabled=false)*");
		log.warn("*  Authentication is still enforced, but fine-grained policy is   *");
		log.warn("*  NOT. Every authenticated request is permitted.                 *");
		log.warn("*  This must NEVER be set in a production deployment.             *");
		log.warn("******************************************************************");
	}
}
