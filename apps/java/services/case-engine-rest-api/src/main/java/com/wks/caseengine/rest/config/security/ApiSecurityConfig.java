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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.wks.api.security.JwksIssuerAuthenticationManagerResolver;
import com.wks.api.security.OpenPolicyAuthzEnforcer;
import com.wks.api.security.OpenPolicyAuthzEnforcerConfig;

@Configuration
@ConditionalOnProperty(name = "wks.security.enabled", havingValue = "true", matchIfMissing = true)
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
		OpenPolicyAuthzEnforcer enforcer = new OpenPolicyAuthzEnforcer(OpenPolicyAuthzEnforcerConfig.builder()
				.opaAuthURL(opaUrl).actuatorEnabled(actuatorEnabled).swaggerEnabled(swaggerEnabled).build());

		http.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authz -> authz.anyRequest().access(enforcer))
				.oauth2ResourceServer(oauth2 -> oauth2
						.authenticationManagerResolver(new JwksIssuerAuthenticationManagerResolver(keycloakUrl)));
		return http.build();
	}

}
