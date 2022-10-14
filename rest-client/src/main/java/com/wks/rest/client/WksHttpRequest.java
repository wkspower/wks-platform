package com.wks.rest.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

/**
 * @author victor.franca
 *
 */
public interface WksHttpRequest {

	HttpMethod getHttpMethod();

	String getHttpRequestUrl();

	HttpEntity<?> getHttpEntity();

}
