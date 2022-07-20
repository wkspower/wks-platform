package com.mmc.bpm.engine.camunda.http.request;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import com.mmc.bpm.rest.client.MmcHttpRequest;

import lombok.Getter;

/**
 * @author victor.franca
 *
 */
@Getter
public class CamundaHttpDeleteRequest implements MmcHttpRequest {

	public String httpRequestUrl;
	public HttpMethod httpMethod;
	public HttpEntity<?> httpEntity;

	public CamundaHttpDeleteRequest(final String url, final HttpEntity<?> httpEntity) {
		this.httpRequestUrl = url;
		this.httpEntity = httpEntity;
		this.httpMethod = HttpMethod.DELETE;
	}

}