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
package com.wks.storage.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Main security filter chain. Both cross-cutting concerns are resolved as
 * strategy beans selected by property in {@code api-security}: the
 * {@link AuthorizationManager} (OPA vs permit-authenticated) and the
 * {@link AuthenticationManagerResolver} (keycloak vs dev-token). This chain
 * autowires the single active bean of each and contains no branch.
 */
@Configuration
@ConditionalOnProperty(name = "wks.security.enabled", havingValue = "true", matchIfMissing = true)
public class ApiSecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
			AuthorizationManager<RequestAuthorizationContext> authorizationManager,
			AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) throws Exception {

		http.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable());

		http.authorizeHttpRequests(authz -> authz.anyRequest().access(authorizationManager));

		http.oauth2ResourceServer(oauth2 -> oauth2
				.authenticationManagerResolver(authenticationManagerResolver));

		return http.build();
	}

}
