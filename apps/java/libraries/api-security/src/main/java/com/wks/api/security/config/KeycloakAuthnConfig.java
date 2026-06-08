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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;

import com.wks.api.security.JwksIssuerAuthenticationManagerResolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Authentication strategy bean for the default Keycloak mode
 * ({@code wks.auth.mode=keycloak} or absent): resolves a per-issuer
 * {@code AuthenticationManager} from the configured JWKS issuer.
 *
 * <p>
 * The dev-token mode ({@code wks.auth.mode=dev-token}) supplies its own resolver
 * bean from {@code DevTokenSecurityConfig}, pointing at the embedded
 * {@code /dev-auth} issuer. The two modes are mutually exclusive, so exactly one
 * {@link AuthenticationManagerResolver} bean exists and the consuming
 * {@code SecurityFilterChain} autowires it without a branch.
 *
 * <p>
 * The issuer URL is unified under {@code wks.auth.issuer-uri}; it falls back to
 * the legacy {@code keycloak.url} key for backward compatibility.
 *
 * @author wks
 */
@Configuration
@ConditionalOnProperty(name = "wks.auth.mode", havingValue = "keycloak", matchIfMissing = true)
public class KeycloakAuthnConfig {

	@Bean
	public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
			@Value("${wks.auth.issuer-uri:${keycloak.url:http://localhost:8082}}") String issuerUri) {
		return new JwksIssuerAuthenticationManagerResolver(issuerUri);
	}

}
