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

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.extern.slf4j.Slf4j;

/**
 * Generates an in-memory RSA keypair used by the embedded dev-token issuer to
 * mint and publish RS256 JWTs. Active only when {@code wks.auth.mode=dev-token}.
 *
 * <p>
 * The keypair lives only for the lifetime of the JVM and is never persisted.
 * This is a development convenience and must never be enabled in production.
 *
 * @author wks
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "wks.auth.mode", havingValue = "dev-token")
public class DevTokenKeyConfig {

	@Bean
	public RSAKey devTokenRsaKey() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		var keyPair = generator.generateKeyPair();

		String kid = "wks-dev-token-" + UUID.randomUUID();

		RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey((RSAPrivateKey) keyPair.getPrivate())
				.keyID(kid)
				.keyUse(KeyUse.SIGNATURE)
				.algorithm(JWSAlgorithm.RS256)
				.build();

		log.warn("DEV-TOKEN issuer generated in-memory RSA signing key (kid={})", kid);
		return rsaKey;
	}

}
