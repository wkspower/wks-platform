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
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.http.HttpEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OpenPolicyAuthzEnforcer implements AuthorizationManager<RequestAuthorizationContext> {

	private static final int OPA_CONNECTION_REQUEST_TIMEOUT_MS = 2000;
	private static final int OPA_READ_TIMEOUT_MS = 3000;

	private final RestTemplate restTemplate;
	private final List<RequestMatcher> matchers;
	private final OpenPolicyAuthzEnforcerConfig config;

	public OpenPolicyAuthzEnforcer(String opaAuthURL) {
		this(OpenPolicyAuthzEnforcerConfig.builder().opaAuthURL(opaAuthURL).build());
	}

	public OpenPolicyAuthzEnforcer(final OpenPolicyAuthzEnforcerConfig config) {
		this.config = config;
		this.restTemplate = createRestTemplate();
		this.matchers = new ArrayList<>();

		PathPatternRequestMatcher.Builder builder = PathPatternRequestMatcher.withDefaults();

		if (config.isActuatorEnabled()) {
			this.matchers.add(builder.matcher("/healthCheck"));
			this.matchers.add(builder.matcher("/actuator/**"));
		}

		if (config.isSwaggerEnabled()) {
			this.matchers.add(builder.matcher("/swagger-ui/**"));
			this.matchers.add(builder.matcher("/swagger-ui.html"));
			this.matchers.add(builder.matcher("/v3/api-docs/**"));
		}
	}

	@Override
	public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication,
			RequestAuthorizationContext context) {
		HttpServletRequest request = context.getRequest();

		if (matchers.stream().anyMatch(m -> m.matches(request))) {
			return new AuthorizationDecision(true);
		}

		Authentication auth;
		try {
			auth = authentication.get();
		} catch (RuntimeException e) {
			log.debug("No authentication available, denying", e);
			return new AuthorizationDecision(false);
		}

		Map<String, Object> input = config.getHandler().resolver(request, auth);

		HttpEntity<?> body = new HttpEntity<>(new OpenPolicyRequest(input));
		OpenPolicyResponse response;
		try {
			response = restTemplate.postForObject(this.config.getOpaAuthURL(), body, OpenPolicyResponse.class);
		} catch (RestClientException e) {
			log.error("OPA call failed; denying request", e);
			throw new AccessDeniedException("Authorization service unavailable", e);
		}

		if (response == null || !response.getResult()) {
			log.debug("Denied with Input -> {}", input);
			return new AuthorizationDecision(false);
		}

		log.debug("Allowed with Input -> {}", input);
		return new AuthorizationDecision(true);
	}

	private RestTemplate createRestTemplate() {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectionRequestTimeout(OPA_CONNECTION_REQUEST_TIMEOUT_MS);
		requestFactory.setReadTimeout(OPA_READ_TIMEOUT_MS);
		return new RestTemplate(requestFactory);
	}

}
