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
import com.wks.caseengine.client.model.Task;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class TaskApi {
	private ApiClient apiClient;

	public TaskApi() {
		this(new ApiClient());
	}

	public TaskApi(ApiClient apiClient) {
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
	 * @param taskId       (required)
	 * @param taskAssignee (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void claim(String taskId, String taskAssignee) throws RestClientException {
		claimWithHttpInfo(taskId, taskAssignee);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param taskId       (required)
	 * @param taskAssignee (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> claimWithHttpInfo(String taskId, String taskAssignee) throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'taskId' is set
		if (taskId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'taskId' when calling claim");
		}

		// verify the required parameter 'taskAssignee' is set
		if (taskAssignee == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'taskAssignee' when calling claim");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("taskId", taskId);
		uriVariables.put("taskAssignee", taskAssignee);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = {};
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {
		};
		return apiClient.invokeAPI("/task/{taskId}/claim/{taskAssignee}", HttpMethod.POST, uriVariables,
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param taskId (required)
	 * @param body   (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void complete(String taskId, String body) throws RestClientException {
		completeWithHttpInfo(taskId, body);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param taskId (required)
	 * @param body   (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> completeWithHttpInfo(String taskId, String body) throws RestClientException {
		Object localVarPostBody = body;

		// verify the required parameter 'taskId' is set
		if (taskId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'taskId' when calling complete");
		}

		// verify the required parameter 'body' is set
		if (body == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'body' when calling complete");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("taskId", taskId);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = {};
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = { "application/json" };
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {
		};
		return apiClient.invokeAPI("/task/{taskId}/complete", HttpMethod.POST, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (optional)
	 * @return List&lt;Task&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public List<Task> find(String businessKey) throws RestClientException {
		return findWithHttpInfo(businessKey).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (optional)
	 * @return ResponseEntity&lt;List&lt;Task&gt;&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<List<Task>> findWithHttpInfo(String businessKey) throws RestClientException {
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

		ParameterizedTypeReference<List<Task>> localReturnType = new ParameterizedTypeReference<List<Task>>() {
		};
		return apiClient.invokeAPI("/task/", HttpMethod.GET, Collections.<String, Object>emptyMap(),
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param taskId (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void unclaim(String taskId) throws RestClientException {
		unclaimWithHttpInfo(taskId);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param taskId (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> unclaimWithHttpInfo(String taskId) throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'taskId' is set
		if (taskId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'taskId' when calling unclaim");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("taskId", taskId);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = {};
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {
		};
		return apiClient.invokeAPI("/task/{taskId}/unclaim", HttpMethod.POST, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}
}
