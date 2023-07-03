package com.wks.caseengine.client.api;

import java.util.Collections;
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
import com.wks.caseengine.client.model.ActivityInstance;
import com.wks.caseengine.client.model.ProcessInstance;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class ProcessInstanceApi {
	private ApiClient apiClient;

	public ProcessInstanceApi() {
		this(new ApiClient());
	}

	public ProcessInstanceApi(ApiClient apiClient) {
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
	 * @param businessKey (optional)
	 * @return List&lt;ProcessInstance&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public List<ProcessInstance> find3(String businessKey) throws RestClientException {
		return find3WithHttpInfo(businessKey).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (optional)
	 * @return ResponseEntity&lt;List&lt;ProcessInstance&gt;&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<List<ProcessInstance>> find3WithHttpInfo(String businessKey) throws RestClientException {
		Object localVarPostBody = null;

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "businessKey", businessKey));

		final String[] localVarAccepts = { "*/*" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<List<ProcessInstance>> localReturnType = new ParameterizedTypeReference<List<ProcessInstance>>() {
		};
		return apiClient.invokeAPI("/process-instance/", HttpMethod.GET, Collections.<String, Object>emptyMap(),
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param id (required)
	 * @return List&lt;ActivityInstance&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public List<ActivityInstance> getActivityInstances(String id) throws RestClientException {
		return getActivityInstancesWithHttpInfo(id).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param id (required)
	 * @return ResponseEntity&lt;List&lt;ActivityInstance&gt;&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<List<ActivityInstance>> getActivityInstancesWithHttpInfo(String id)
			throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'id' is set
		if (id == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'id' when calling getActivityInstances");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", id);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "*/*" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<List<ActivityInstance>> localReturnType = new ParameterizedTypeReference<List<ActivityInstance>>() {
		};
		return apiClient.invokeAPI("/process-instance/{id}/activity-instances", HttpMethod.GET, uriVariables,
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}
}
