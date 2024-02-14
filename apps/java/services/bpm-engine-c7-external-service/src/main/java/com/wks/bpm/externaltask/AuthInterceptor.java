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

import java.io.IOException;

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
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonParser;

/**
 * @author victor.franca
 *
 */
@Component
public class AuthInterceptor implements ClientHttpRequestInterceptor {

	@Value("${wks-case-api.auth.url}")
	private String authUrl;

	@Value("${wks-case-api.auth.client_id}")
	private String clientId;

	@Value("${wks-case-api.auth.client_secret}")
	private String clientSecret;

	@Value("${wks-case-api.auth.grant_type}")
	private String grantType;

	@Override
	public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
			final ClientHttpRequestExecution execution) throws IOException {
		HttpHeaders headers = request.getHeaders();
		headers.add("Authorization", "Bearer " + getBearerToken());
		return execution.execute(request, body);
	}

	private String getBearerToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("client_id", clientId);
		requestBody.add("client_secret", clientSecret);
		requestBody.add("grant_type", grantType);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<String> responseEntity = restTemplate.postForEntity(authUrl, request, String.class);

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			String responseBody = responseEntity.getBody();
			String accessToken = JsonParser.parseString(responseBody).getAsJsonObject().get("access_token")
					.getAsString();
			return accessToken;
		} else {
			throw new RuntimeException("Failed to retrieve token");
		}
	}

}
