package com.wks.rest.client.auth;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import com.wks.rest.client.WksHttpRequest;

import lombok.Getter;

/**
 * @author victor.franca
 *
 */
@Getter
public class TokenHttpPostRequest implements WksHttpRequest {

	public String httpRequestUrl;
	public HttpMethod httpMethod;
	public HttpEntity<?> httpEntity;

	public TokenHttpPostRequest(final String baseUri, final HttpEntity<?> httpEntity) {
		this.httpRequestUrl = baseUri;
		this.httpEntity = httpEntity;
		this.httpMethod = HttpMethod.POST;
	}

}
