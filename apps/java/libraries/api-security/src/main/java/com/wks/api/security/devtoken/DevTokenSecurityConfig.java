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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

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

}
