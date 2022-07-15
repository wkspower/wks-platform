package com.mmc.bpm.engine.camunda.http.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mmc.bpm.rest.client.header.HttpHeadersFactory;

/**
 * @author victor.franca
 *
 */
@Component
public class CamundaOAuthHttpHeadersFactory implements HttpHeadersFactory {

	@Autowired
	private CamundaTokenHttpPostRequestFactory tokenPostRequestFactory;

	@Autowired
	private RestTemplate restTemplate;

	private String token;

	@Override
	public HttpHeaders create() {
		HttpHeaders headers = new HttpHeaders();

		headers.set("Authorization", getToken());

		return headers;
	}

	/**
	 * @author victor.franca
	 *
	 * @return the Bearer Token
	 */
	private String getToken() {

		// TODO condition should embrace timed out token
//		if (token == null) {
//			MmcHttpRequest request = tokenPostRequestFactory.create();
//
//			token = String.valueOf(((JsonObject) restTemplate.exchange(request.getHttpRequestUrl(),
//					request.getHttpMethod(), request.getHttpEntity(), JsonObject.class).getBody()).get("token"));
//		}
		return "Bearer " + token;
	}

}
