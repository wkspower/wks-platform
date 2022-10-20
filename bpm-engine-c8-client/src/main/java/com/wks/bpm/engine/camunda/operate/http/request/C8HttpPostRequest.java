package com.wks.bpm.engine.camunda.operate.http.request;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import com.wks.rest.client.WksHttpRequest;

import lombok.Getter;

/**
 * @author victor.franca
 *
 */
@Getter
public class C8HttpPostRequest implements WksHttpRequest {

	public String httpRequestUrl;
	public HttpMethod httpMethod;
	public HttpEntity<?> httpEntity;

	public C8HttpPostRequest(final String url, final HttpEntity<?> httpEntity) {
		this.httpRequestUrl = url;
		this.httpEntity = httpEntity;
		this.httpMethod = HttpMethod.POST;
	}

}
