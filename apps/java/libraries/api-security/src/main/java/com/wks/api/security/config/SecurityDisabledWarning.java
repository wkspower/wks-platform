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
 * Logs a loud warning at startup when {@code wks.security.enabled=false} so an
 * accidentally-disabled security filter chain (intended only for @WebMvcTest
 * slices) is impossible to miss in container logs.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "wks.security.enabled", havingValue = "false")
public class SecurityDisabledWarning {

	@PostConstruct
	public void warn() {
		log.warn("******************************************************************");
		log.warn("*  WKS SECURITY IS DISABLED (wks.security.enabled=false)         *");
		log.warn("*  All HTTP endpoints are unauthenticated.                       *");
		log.warn("*  This must NEVER be set in a production deployment.            *");
		log.warn("******************************************************************");
	}
}
