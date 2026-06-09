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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.wks.api.security.JwksIssuerAuthenticationManagerResolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Dedicated security filter chain exposing the embedded dev-token issuer
 * endpoints ({@code /dev-auth/**}) as public, without modifying the main
 * {@code ApiSecurityConfig}. Active only when {@code wks.auth.mode=dev-token}.
 *
 * <p>
 * Ordered first so it takes precedence over the application's authenticated
 * filter chain for matching requests.
 *
 * @author wks
 */
@Configuration
@ConditionalOnProperty(name = "wks.auth.mode", havingValue = "dev-token")
public class DevTokenSecurityConfig {

	@Bean
	@Order(1)
	public SecurityFilterChain devAuthFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/dev-auth/**")
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
		return http.build();
	}

	/**
	 * Authentication resolver for dev-token mode, pointing at the embedded
	 * {@code /dev-auth} issuer. Replaces the {@code KeycloakAuthnConfig} bean
	 * (which is inactive in dev-token mode) so the main filter chain autowires a
	 * single resolver without branching on the auth mode.
	 *
	 * <p>
	 * Binds {@code wks.auth.devtoken.issuer-uri} (its own key, distinct from the
	 * Keycloak {@code wks.auth.issuer-uri}) because validation fetches the JWKS over
	 * the network: in a multi-container deployment a non-issuing service (e.g.
	 * storage-api) must reach the issuing engine by its service name, not localhost.
	 * The default lives in {@code api-security-defaults.properties}.
	 */
	@Bean
	public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
			@Value("${wks.auth.devtoken.issuer-uri}") String issuerUri) {
		return new JwksIssuerAuthenticationManagerResolver(issuerUri);
	}

}
