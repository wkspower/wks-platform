package com.wks.bpm.engine.camunda.http.request;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import com.wks.rest.client.WksHttpRequest;

import lombok.Getter;

/**
 * @author victor.franca
 *
 */
@Getter
public class C7HttpGetRequest<T> implements WksHttpRequest {

	public String httpRequestUrl;
	public HttpMethod httpMethod;
	public HttpEntity<T> httpEntity;

	public C7HttpGetRequest(final String url, final HttpEntity<T> httpEntity) {
		this.httpRequestUrl = url;
		this.httpEntity = httpEntity;
		this.httpMethod = HttpMethod.GET;
	}

}
