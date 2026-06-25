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
package com.wks.api.security;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Authorization strategy used when authorization (OPA) is disabled
 * ({@code wks.authz.opa.enabled=false}): actuator/swagger endpoints are
 * permitted (when their feature flags are on) and every other request only
 * requires an authenticated (non-anonymous) principal. Authentication itself
 * stays enforced by the resource server.
 *
 * <p>
 * Mirrors the permit matchers carried by {@link OpenPolicyAuthzEnforcer} so the
 * two strategies expose the same public surface.
 *
 * @author wks
 */
@Slf4j
public final class PermitAuthenticatedAuthorizationManager
		implements AuthorizationManager<RequestAuthorizationContext> {

	private final List<RequestMatcher> permitMatchers;

	public PermitAuthenticatedAuthorizationManager(boolean actuatorEnabled, boolean swaggerEnabled) {
		this.permitMatchers = new ArrayList<>();

		PathPatternRequestMatcher.Builder builder = PathPatternRequestMatcher.withDefaults();

		if (actuatorEnabled) {
			this.permitMatchers.add(builder.matcher("/healthCheck"));
			this.permitMatchers.add(builder.matcher("/actuator/**"));
		}

		if (swaggerEnabled) {
			this.permitMatchers.add(builder.matcher("/swagger-ui/**"));
			this.permitMatchers.add(builder.matcher("/swagger-ui.html"));
			this.permitMatchers.add(builder.matcher("/v3/api-docs/**"));
		}
	}

	@Override
	public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication,
			RequestAuthorizationContext context) {
		HttpServletRequest request = context.getRequest();

		if (permitMatchers.stream().anyMatch(m -> m.matches(request))) {
			return new AuthorizationDecision(true);
		}

		Authentication auth;
		try {
			auth = authentication.get();
		} catch (RuntimeException e) {
			log.debug("No authentication available, denying", e);
			return new AuthorizationDecision(false);
		}

		boolean authenticated = auth != null && auth.isAuthenticated()
				&& !(auth instanceof AnonymousAuthenticationToken);
		return new AuthorizationDecision(authenticated);
	}

}
