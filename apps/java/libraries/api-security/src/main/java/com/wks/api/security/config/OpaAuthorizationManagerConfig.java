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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.wks.api.security.BearerTokenHandlerInputResolver;
import com.wks.api.security.OpenPolicyAuthzEnforcer;
import com.wks.api.security.OpenPolicyAuthzEnforcerConfig;

/**
 * Authorization strategy bean for the default, fail-secure mode where Open
 * Policy Agent (OPA) enforces every request. Selected when
 * {@code wks.authz.opa.enabled} is {@code true} or absent.
 *
 * <p>
 * The consuming {@code SecurityFilterChain} autowires the single active
 * {@link AuthorizationManager} and contains no branch; the alternative strategy
 * is {@code PermitAuthenticatedAuthorizationManagerConfig}.
 *
 * @author wks
 */
@Configuration
// Requires spring-security, a {@code provided}-scope dependency of this library. Consumers that
// do not bundle it (e.g. the headless c7-external-tasks worker) must skip this config; the
// condition is read from bytecode metadata so it never loads AuthorizationManager directly.
@ConditionalOnClass(AuthorizationManager.class)
@ConditionalOnProperty(name = "wks.authz.opa.enabled", havingValue = "true", matchIfMissing = true)
public class OpaAuthorizationManagerConfig {

	@Bean
	public AuthorizationManager<RequestAuthorizationContext> authorizationManager(
			@Value("${opa.url}") String opaUrl,
			@Value("${wks.api.actuator.enabled:true}") boolean actuatorEnabled,
			@Value("${wks.api.swagger.enabled:true}") boolean swaggerEnabled,
			@Value("${wks.tenancy.claim-name}") String tenantClaim) {

		return new OpenPolicyAuthzEnforcer(OpenPolicyAuthzEnforcerConfig.builder()
				.opaAuthURL(opaUrl)
				.handler(new BearerTokenHandlerInputResolver(tenantClaim))
				.actuatorEnabled(actuatorEnabled)
				.swaggerEnabled(swaggerEnabled)
				.build());
	}

}
