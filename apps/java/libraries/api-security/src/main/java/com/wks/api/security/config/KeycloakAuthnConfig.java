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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
 * The issuer URL is unified under {@code wks.auth.issuer-uri}; its default lives
 * in the library {@code api-security-defaults.properties} (sourced from the
 * {@code KEYCLOAK_URL} env var) so it is not hardcoded here.
 *
 * @author wks
 */
@Configuration
// spring-security is a {@code provided}-scope dependency of this library, so consumers that do
// not bundle it (e.g. the headless c7-external-tasks worker, which only needs the tenant holder)
// must not load this config. {@code @ConditionalOnClass} is read from bytecode metadata, so it
// skips the class without triggering NoClassDefFoundError on AuthenticationManagerResolver.
@ConditionalOnClass(AuthenticationManagerResolver.class)
@ConditionalOnProperty(name = "wks.auth.mode", havingValue = "keycloak", matchIfMissing = true)
public class KeycloakAuthnConfig {

	@Bean
	public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
			@Value("${wks.auth.issuer-uri}") String issuerUri) {
		return new JwksIssuerAuthenticationManagerResolver(issuerUri);
	}

}
