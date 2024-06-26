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
package com.wks.caseengine.rest.config.security;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.wks.api.security.JwksIssuerAuthenticationManagerResolver;
import com.wks.api.security.OpenPolicyAuthzEnforcer;
import com.wks.api.security.OpenPolicyAuthzEnforcerConfig;

@Configuration
public class ApiSecurityConfig {

	@Value("${opa.url}")
	private String opaUrl;

	@Value("${keycloak.url}")
	private String keycloakUrl;

	@Value("${case.engine.actuator.enabled}")
	private Boolean actuatorEnabled;

	@Value("${case.engine.swagger.enabled}")
	private Boolean swaggerEnabled;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable()
				.authorizeRequests(authz -> authz.filterSecurityInterceptorOncePerRequest(false).anyRequest()
						.authenticated().accessDecisionManager(accessDecisionManager()))
				.oauth2ResourceServer(oauth2 -> oauth2
						.authenticationManagerResolver(new JwksIssuerAuthenticationManagerResolver(keycloakUrl)));
		return http.build();
	}

	public AccessDecisionManager accessDecisionManager() {
		return new UnanimousBased(Arrays.asList(new OpenPolicyAuthzEnforcer(OpenPolicyAuthzEnforcerConfig.builder()
				.opaAuthURL(opaUrl).actuatorEnabled(actuatorEnabled).swaggerEnabled(swaggerEnabled).build())));
	}

}