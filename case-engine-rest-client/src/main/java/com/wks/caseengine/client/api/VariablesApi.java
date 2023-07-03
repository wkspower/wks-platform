package com.wks.caseengine.client.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.wks.caseengine.client.invoker.ApiClient;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class VariablesApi {
	private ApiClient apiClient;

	public VariablesApi() {
		this(new ApiClient());
	}

	public VariablesApi(ApiClient apiClient) {
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
	 * @param processInstanceId (required)
	 * @return String
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public String findVariables(String processInstanceId) throws RestClientException {
		return findVariablesWithHttpInfo(processInstanceId).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param processInstanceId (required)
	 * @return ResponseEntity&lt;String&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<String> findVariablesWithHttpInfo(String processInstanceId) throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'processInstanceId' is set
		if (processInstanceId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'processInstanceId' when calling findVariables");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("processInstanceId", processInstanceId);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "application/json" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<String> localReturnType = new ParameterizedTypeReference<String>() {
		};
		return apiClient.invokeAPI("/variable/{processInstanceId}", HttpMethod.GET, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}
}
