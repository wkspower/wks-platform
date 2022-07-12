package com.mmc.bpm.rest.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

/**
 * @author victor.franca
 *
 */
public interface MmcHttpRequest {

	public HttpMethod getHttpMethod();

	public String getHttpRequestUrl();

	public HttpEntity<?> getHttpEntity();

}
