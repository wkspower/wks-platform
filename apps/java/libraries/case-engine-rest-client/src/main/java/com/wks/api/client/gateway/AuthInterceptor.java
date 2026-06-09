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
package com.wks.api.client.gateway;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Adds a Keycloak bearer token to every outbound case-api request.
 * <p>
 * The token is cached and reused until shortly before it expires, instead of being re-fetched
 * on every request. This removes a full Keycloak round-trip from each external-task execution
 * (the original implementation POSTed to the token endpoint on every call with a fresh, unpooled
 * {@link RestTemplate}). The token endpoint itself is now called through a pooled HTTP client
 * with explicit timeouts, so a slow/stale Keycloak connection cannot hang a worker indefinitely.
 *
 * @author victor.franca
 */
@Component
public class AuthInterceptor implements ClientHttpRequestInterceptor {

	private static final int TOKEN_CONNECT_TIMEOUT_MS = 2000;
	private static final int TOKEN_CONNECTION_REQUEST_TIMEOUT_MS = 2000;
	private static final int TOKEN_SOCKET_TIMEOUT_MS = 5000;

	/** Refresh the token this many ms before it actually expires, to absorb clock skew / in-flight calls. */
	private static final long TOKEN_EXPIRY_SKEW_MS = 30_000L;

	/** Fallback TTL when the token response has no {@code expires_in}. */
	private static final long DEFAULT_TOKEN_TTL_MS = 60_000L;

	@Value("${wks-case-api.auth.url}")
	private String authUrl;

	@Value("${wks-case-api.auth.client_id}")
	private String clientId;

	@Value("${wks-case-api.auth.client_secret}")
	private String clientSecret;

	@Value("${wks-case-api.auth.grant_type}")
	private String grantType;

	/** Dedicated, pooled client for the token endpoint (separate from the case-api RestTemplate). */
	private final RestTemplate tokenRestTemplate = createTokenRestTemplate();

	private final Object tokenLock = new Object();
	private volatile String cachedToken;
	private volatile long tokenExpiresAtMillis = 0L;

	@Override
	public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
			final ClientHttpRequestExecution execution) throws IOException {
		request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + getBearerToken());
		return execution.execute(request, body);
	}

	/**
	 * Eagerly fetch and cache a token. Intended to be called at application startup so the first
	 * real request does not pay the cold-start cost (TLS handshake + token fetch against Keycloak).
	 */
	public void prefetchToken() {
		getBearerToken();
	}

	private String getBearerToken() {
		String token = cachedToken;
		if (token != null && System.currentTimeMillis() < tokenExpiresAtMillis) {
			return token;
		}
		synchronized (tokenLock) {
			if (cachedToken != null && System.currentTimeMillis() < tokenExpiresAtMillis) {
				return cachedToken;
			}
			return fetchAndCacheToken();
		}
	}

	private String fetchAndCacheToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("client_id", clientId);
		requestBody.add("client_secret", clientSecret);
		requestBody.add("grant_type", grantType);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<String> responseEntity = tokenRestTemplate.postForEntity(authUrl, request, String.class);

		if (responseEntity.getStatusCode() != HttpStatus.OK) {
			throw new RuntimeException("Failed to retrieve token");
		}

		JsonObject json = JsonParser.parseString(responseEntity.getBody()).getAsJsonObject();
		String accessToken = json.get("access_token").getAsString();

		long ttlMs = json.has("expires_in") ? json.get("expires_in").getAsLong() * 1000L : DEFAULT_TOKEN_TTL_MS;
		long effectiveTtlMs = Math.max(ttlMs - TOKEN_EXPIRY_SKEW_MS, 0L);

		cachedToken = accessToken;
		tokenExpiresAtMillis = System.currentTimeMillis() + effectiveTtlMs;
		return accessToken;
	}

	private static RestTemplate createTokenRestTemplate() {
		ConnectionConfig connectionConfig = ConnectionConfig.custom()
				.setConnectTimeout(Timeout.of(TOKEN_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS))
				.setSocketTimeout(Timeout.of(TOKEN_SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS))
				.build();

		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setDefaultConnectionConfig(connectionConfig)
				.build();

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(Timeout.of(TOKEN_CONNECTION_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS))
				.build();

		CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.setDefaultRequestConfig(requestConfig)
				.build();

		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
	}

}
