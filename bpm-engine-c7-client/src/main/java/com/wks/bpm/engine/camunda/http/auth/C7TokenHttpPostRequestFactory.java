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
package com.wks.bpm.engine.camunda.http.auth;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.wks.rest.client.WksHttpRequest;
import com.wks.rest.client.auth.TokenHttpPostRequest;
import com.wks.rest.client.auth.TokenHttpPostRequestFactory;

/**
 * @author victor.franca
 *
 */
@Component
public class C7TokenHttpPostRequestFactory implements TokenHttpPostRequestFactory {

	@Value("${camunda7.rest.oauth.token.uri}")
	private String tokenUri;

	@Value("${camunda7.rest.oauth.token.clientId}")
	private String tokenClientId;

	@Value("${camunda7.rest.oauth.token.clientSecret}")
	private String tokenClientSecret;

	@Value("${camunda7.rest.oauth.token.grantType}")
	private String tokenGrantType;

	@Override
	public WksHttpRequest create() {
		Map<String, String> tokenRequest = new LinkedHashMap<>();
		tokenRequest.put("grant_type", tokenGrantType);
		tokenRequest.put("client_id", tokenClientId);
		tokenRequest.put("client_secret", tokenClientSecret);

		return new TokenHttpPostRequest(tokenUri, new HttpEntity<>(tokenRequest, new HttpHeaders()));
	}

}
