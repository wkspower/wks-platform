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

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Builds the case-api {@link RestTemplate} backed by a pooled HTTP client with explicit
 * connect/socket timeouts. Connection pooling avoids per-call connection warmup (notably on the
 * cold first request), and the socket timeout prevents a stale/hung case-api connection from
 * blocking a worker indefinitely.
 *
 * @author victor.franca
 */
@Configuration
public class RestTemplateConfig {

	private static final int CONNECT_TIMEOUT_MS = 5000;
	private static final int CONNECTION_REQUEST_TIMEOUT_MS = 5000;
	private static final int SOCKET_TIMEOUT_MS = 30000;

	@Autowired
	AuthInterceptor authInterceptor;

	@Bean
	public RestTemplate getRestTemplate() {
		ConnectionConfig connectionConfig = ConnectionConfig.custom()
				.setConnectTimeout(Timeout.of(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS))
				.setSocketTimeout(Timeout.of(SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS))
				.build();

		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setDefaultConnectionConfig(connectionConfig)
				.build();

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(Timeout.of(CONNECTION_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS))
				.build();

		CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.setDefaultRequestConfig(requestConfig)
				.build();

		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
		restTemplate.setInterceptors(Collections.singletonList(authInterceptor));
		return restTemplate;
	}

}
