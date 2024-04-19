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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OpenPolicyAuthzEnforcer implements AccessDecisionVoter<Object> {

	private RestTemplate restTemplate;
	private List<AntPathRequestMatcher> matchers;

	private OpenPolicyAuthzEnforcerConfig config;

	public OpenPolicyAuthzEnforcer(String opaAuthURL) {
		this(OpenPolicyAuthzEnforcerConfig.builder().opaAuthURL(opaAuthURL).build());
	}

	public OpenPolicyAuthzEnforcer(final OpenPolicyAuthzEnforcerConfig config) {
		this.config = config;
		this.restTemplate = createRestTemplate();
		this.matchers = new LinkedList<>();

		if (config.isActuatorEnabled()) {
			this.matchers.addAll(Arrays.asList(new AntPathRequestMatcher("/healthCheck"),
					new AntPathRequestMatcher("/actuator/**")));
		}

		if (config.isSwaggerEnabled()) {
			this.matchers.addAll(Arrays.asList(new AntPathRequestMatcher("/swagger-ui/**"),
					new AntPathRequestMatcher("/swagger-ui.html"), new AntPathRequestMatcher("/v3/api-docs/**")));
		}
	}

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public int vote(Authentication authentication, Object obj, Collection<ConfigAttribute> attributes) {
		if (!(obj instanceof FilterInvocation)) {
			return ACCESS_ABSTAIN;
		}

		FilterInvocation filter = (FilterInvocation) obj;
		HttpServletRequest request = filter.getRequest();

		if (matchers.stream().filter(f -> f.matches(request)).count() > 0) {
			return ACCESS_GRANTED;
		}

		Map<String, Object> input = config.getHandler().resolver(request, authentication);

		HttpEntity<?> body = new HttpEntity<>(new OpenPolicyRequest(input));
		OpenPolicyResponse response = restTemplate.postForObject(this.config.getOpaAuthURL(), body,
				OpenPolicyResponse.class);
		if (response == null) {
			throw new RuntimeException("Error connecting to OPA Server");
		}

		if (!response.getResult()) {
			log.debug("Denied with Input -> {}", input);
			return ACCESS_DENIED;
		}

		log.debug("Allowed with Input -> {}", input);
		return ACCESS_GRANTED;
	}

	private RestTemplate createRestTemplate() {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		return new RestTemplate(requestFactory);
	}

}