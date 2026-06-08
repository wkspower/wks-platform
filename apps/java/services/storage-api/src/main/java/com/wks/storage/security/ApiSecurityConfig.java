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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

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

	@Value("${storage.actuator.enabled:true}")
	private Boolean actuatorEnabled;

	@Value("${storage.swagger.enabled:true}")
	private Boolean swaggerEnabled;

	// Authorization (OPA) is orthogonal to authentication. When false the OPA
	// enforcer is never instantiated (so it cannot fail closed) and authenticated
	// requests are permitted. Authentication (oauth2ResourceServer) stays on.
	@Value("${wks.authz.opa.enabled:true}")
	private Boolean opaEnabled;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable());

		if (Boolean.TRUE.equals(opaEnabled)) {
			OpenPolicyAuthzEnforcer enforcer = new OpenPolicyAuthzEnforcer(OpenPolicyAuthzEnforcerConfig.builder()
					.opaAuthURL(opaUrl).actuatorEnabled(actuatorEnabled).swaggerEnabled(swaggerEnabled).build());
			http.authorizeHttpRequests(authz -> authz.anyRequest().access(enforcer));
		} else {
			http.authorizeHttpRequests(authz -> {
				PathPatternRequestMatcher.Builder m = PathPatternRequestMatcher.withDefaults();
				if (Boolean.TRUE.equals(actuatorEnabled)) {
					authz.requestMatchers(m.matcher("/healthCheck"), m.matcher("/actuator/**")).permitAll();
				}
				if (Boolean.TRUE.equals(swaggerEnabled)) {
					authz.requestMatchers(m.matcher("/swagger-ui/**"), m.matcher("/swagger-ui.html"),
							m.matcher("/v3/api-docs/**")).permitAll();
				}
				authz.anyRequest().authenticated();
			});
		}

		http.oauth2ResourceServer(oauth2 -> oauth2
				.authenticationManagerResolver(new JwksIssuerAuthenticationManagerResolver(keycloakUrl)));
		return http.build();
	}

}
