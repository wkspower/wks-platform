package com.wks.caseengine.client.api;

import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import com.wks.caseengine.client.invoker.ApiClient;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class HealtcheckApi {
	private ApiClient apiClient;

	public HealtcheckApi() {
		this(new ApiClient());
	}

	public HealtcheckApi(ApiClient apiClient) {
		this.apiClient = apiClient;
	}

	public ApiClient getApiClient() {
		return apiClient;
	}

	public void setApiClient(ApiClient apiClient) {
		this.apiClient = apiClient;
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @return String
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public String check() throws RestClientException {
		return checkWithHttpInfo().getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @return ResponseEntity&lt;String&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<String> checkWithHttpInfo() throws RestClientException {
		Object localVarPostBody = null;

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "*/*" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<String> localReturnType = new ParameterizedTypeReference<String>() {
		};
		return apiClient.invokeAPI("/healthCheck/", HttpMethod.GET, Collections.<String, Object>emptyMap(),
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}
}
