package com.mmc.automation.process.engine.camunda.http.request;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import com.mmc.automation.rest.client.MmcHttpRequest;

import lombok.Getter;

/**
 * @author victor.franca
 *
 */
@Getter
public class CamundaHttpGetRequest<T> implements MmcHttpRequest {

	public String httpRequestUrl;
	public HttpMethod httpMethod;
	public HttpEntity<T> httpEntity;

	public CamundaHttpGetRequest(String url, HttpEntity<T> httpEntity) {
		this.httpRequestUrl = url;
		this.httpEntity = httpEntity;
		this.httpMethod = HttpMethod.GET;
	}

}
