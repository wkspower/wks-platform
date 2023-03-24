package com.wks.bpm.engine.camunda.http.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.wks.rest.client.header.HttpHeadersFactory;

/**
 * @author victor.franca
 *
 */
@Component
public class C7OAuthHttpHeadersFactory extends HttpHeadersFactory {

//	@Autowired
//	private C7TokenHttpPostRequestFactory tokenPostRequestFactory;
//
//	@Autowired
//	private RestTemplate restTemplate;

	private String token;

	@Override
	public HttpHeaders json() {
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
//			WksHttpRequest request = tokenPostRequestFactory.create();
//
//			token = String.valueOf(((JsonObject) restTemplate.exchange(request.getHttpRequestUrl(),
//					request.getHttpMethod(), request.getHttpEntity(), JsonObject.class).getBody()).get("token"));
//		}
		return "Bearer " + token;
	}

}
