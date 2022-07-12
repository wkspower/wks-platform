package com.mmc.automation.rest.client.auth;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import com.mmc.automation.rest.client.MmcHttpRequest;

import lombok.Getter;

/**
 * @author victor.franca
 *
 */
@Getter
public class TokenHttpPostRequest implements MmcHttpRequest {

	public String httpRequestUrl;
	public HttpMethod httpMethod;
	public HttpEntity<?> httpEntity;

	public TokenHttpPostRequest(String baseUri, HttpEntity<?> httpEntity) {
		this.httpRequestUrl = baseUri;
		this.httpEntity = httpEntity;
		this.httpMethod = HttpMethod.POST;
	}

}
