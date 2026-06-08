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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.wks.api.security.PermitAuthenticatedAuthorizationManager;

/**
 * Authorization strategy bean for the OPA-disabled mode
 * ({@code wks.authz.opa.enabled=false}): authenticated requests are permitted
 * and the OPA enforcer is never instantiated (so it cannot fail closed).
 * Authentication stays enforced by the resource server.
 *
 * <p>
 * Mutually exclusive with {@code OpaAuthorizationManagerConfig}; the consuming
 * {@code SecurityFilterChain} autowires the single active
 * {@link AuthorizationManager}.
 *
 * @author wks
 */
@Configuration
@ConditionalOnProperty(name = "wks.authz.opa.enabled", havingValue = "false")
public class PermitAuthenticatedAuthorizationManagerConfig {

	@Bean
	public AuthorizationManager<RequestAuthorizationContext> authorizationManager(
			@Value("${wks.api.actuator.enabled:true}") boolean actuatorEnabled,
			@Value("${wks.api.swagger.enabled:true}") boolean swaggerEnabled) {

		return new PermitAuthenticatedAuthorizationManager(actuatorEnabled, swaggerEnabled);
	}

}
