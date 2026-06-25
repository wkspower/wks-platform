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
package com.wks.bpm.externaltask;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.wks.api.client.gateway.AuthInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Pre-fetches the case-api Keycloak token at startup so the first external task does not pay the
 * cold-start cost (JVM warmup + TLS handshake + token fetch) while holding its lock. Failure here
 * is non-fatal: the first real request will fetch a token on demand.
 *
 * @author victor.franca
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaseApiClientWarmUp {

	private final AuthInterceptor authInterceptor;

	@EventListener(ApplicationReadyEvent.class)
	public void warmUp() {
		try {
			authInterceptor.prefetchToken();
			log.info("Pre-fetched case-api auth token at startup");
		} catch (Exception e) {
			log.warn("Startup case-api token pre-fetch failed; first task will fetch on demand", e);
		}
	}

}
