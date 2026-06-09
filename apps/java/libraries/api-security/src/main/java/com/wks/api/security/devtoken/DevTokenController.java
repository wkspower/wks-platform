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

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

/**
 * Embedded development token issuer that mimics the subset of the Keycloak realm
 * endpoints required by {@code JwksIssuerAuthenticationManagerResolver}. Active
 * only when {@code wks.auth.mode=dev-token}.
 *
 * <p>
 * It publishes the in-memory RSA public key as a JWKS and mints real RS256 JWTs
 * carrying the {@code org} claim so the existing resource-server resolver can
 * validate them unchanged. This lets the platform run without Keycloak. It must
 * never be enabled in production.
 *
 * @author wks
 */
@Slf4j
@RestController
@RequestMapping("/dev-auth")
@ConditionalOnProperty(name = "wks.auth.mode", havingValue = "dev-token")
public class DevTokenController {

	private static final long EXPIRES_IN_SECONDS = 3600L;

	private final RSAKey rsaKey;

	private final String defaultRoles;

	private final List<String> allowedOrigins;

	public DevTokenController(RSAKey devTokenRsaKey,
			@Value("${wks.auth.devtoken.roles}") String defaultRoles,
			@Value("${wks.auth.devtoken.allowed-origins}") String allowedOrigins) {
		this.rsaKey = devTokenRsaKey;
		this.defaultRoles = defaultRoles;
		this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(o -> !o.isEmpty())
				.toList();
	}

	/**
	 * Serves the JWKS containing the RSA public key for the given tenant/realm.
	 * Mirrors {@code /realms/{realm}/protocol/openid-connect/certs}.
	 */
	@GetMapping("/realms/{tenant}/protocol/openid-connect/certs")
	public Map<String, Object> certs(@PathVariable String tenant) {
		log.debug("DEV-TOKEN serving JWKS for tenant '{}'", tenant);
		return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
	}

	/**
	 * Mints a signed RS256 access token for the given tenant/realm.
	 *
	 * @param tenant realm/org the token is issued for (from the path)
	 * @param sub    subject / username (defaults to {@code developer})
	 * @param roles  comma-separated realm roles (defaults to
	 *               {@code wks.auth.devtoken.roles}, itself defaulting to the realm
	 *               roles the portal authorizes against so the dev user is fully usable)
	 */
	@GetMapping("/realms/{tenant}/protocol/openid-connect/token")
	public Map<String, Object> token(@PathVariable String tenant,
			@RequestParam(name = "sub", defaultValue = "developer") String sub,
			@RequestParam(name = "roles", required = false) String roles) throws JOSEException {

		String effectiveRoles = (roles == null || roles.isBlank()) ? defaultRoles : roles;
		List<String> roleList = Arrays.stream(effectiveRoles.split(","))
				.map(String::trim)
				.filter(r -> !r.isEmpty())
				.toList();

		String issuer = String.format("%s/realms/%s", issuerBase(), tenant);

		Instant now = Instant.now();
		Instant exp = now.plusSeconds(EXPIRES_IN_SECONDS);

		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.issuer(issuer)
				.subject(sub)
				.claim("org", tenant)
				.claim("preferred_username", sub)
				.claim("realm_access", Map.of("roles", roleList))
				// Keycloak tokens carry allowed-origins; BearerTokenHandlerInputResolver
				// reads allowed-origins[0], so the dev token must provide it too.
				.claim("allowed-origins", allowedOrigins)
				.issueTime(Date.from(now))
				.expirationTime(Date.from(exp))
				.build();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
				.keyID(rsaKey.getKeyID())
				.type(com.nimbusds.jose.JOSEObjectType.JWT)
				.build();

		SignedJWT signedJWT = new SignedJWT(header, claims);
		signedJWT.sign(new RSASSASigner(rsaKey.toRSAPrivateKey()));

		log.debug("DEV-TOKEN minted token for sub='{}' tenant='{}' roles={}", sub, tenant, roleList);

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("access_token", signedJWT.serialize());
		response.put("token_type", "Bearer");
		response.put("expires_in", EXPIRES_IN_SECONDS);
		return response;
	}

	/**
	 * Derives the issuer base URL from the current request so that the {@code iss}
	 * claim and the JWKS endpoint resolve to the same host.
	 */
	private String issuerBase() {
		return ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/dev-auth")
				.build()
				.toUriString();
	}

}
