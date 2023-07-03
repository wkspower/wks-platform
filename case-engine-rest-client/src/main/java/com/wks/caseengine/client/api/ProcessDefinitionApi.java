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
import com.wks.caseengine.client.model.ProcessDefinition;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class ProcessDefinitionApi {
	private ApiClient apiClient;

	public ProcessDefinitionApi() {
		this(new ApiClient());
	}

	public ProcessDefinitionApi(ApiClient apiClient) {
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
	 * @return List&lt;ProcessDefinition&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public List<ProcessDefinition> find4() throws RestClientException {
		return find4WithHttpInfo().getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @return ResponseEntity&lt;List&lt;ProcessDefinition&gt;&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<List<ProcessDefinition>> find4WithHttpInfo() throws RestClientException {
		Object localVarPostBody = null;

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "application/json" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<List<ProcessDefinition>> localReturnType = new ParameterizedTypeReference<List<ProcessDefinition>>() {
		};
		return apiClient.invokeAPI("/process-definition/", HttpMethod.GET, Collections.<String, Object>emptyMap(),
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param processDefinitionId (required)
	 * @return String
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public String get2(String processDefinitionId) throws RestClientException {
		return get2WithHttpInfo(processDefinitionId).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param processDefinitionId (required)
	 * @return ResponseEntity&lt;String&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<String> get2WithHttpInfo(String processDefinitionId) throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'processDefinitionId' is set
		if (processDefinitionId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'processDefinitionId' when calling get2");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("processDefinitionId", processDefinitionId);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "application/xml" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<String> localReturnType = new ParameterizedTypeReference<String>() {
		};
		return apiClient.invokeAPI("/process-definition/{processDefinitionId}/xml", HttpMethod.GET, uriVariables,
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param bpmEngineId         (required)
	 * @param processDefinitionId (required)
	 * @return String
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public String get3(String bpmEngineId, String processDefinitionId) throws RestClientException {
		return get3WithHttpInfo(bpmEngineId, processDefinitionId).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param bpmEngineId         (required)
	 * @param processDefinitionId (required)
	 * @return ResponseEntity&lt;String&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<String> get3WithHttpInfo(String bpmEngineId, String processDefinitionId)
			throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'bpmEngineId' is set
		if (bpmEngineId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'bpmEngineId' when calling get3");
		}

		// verify the required parameter 'processDefinitionId' is set
		if (processDefinitionId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'processDefinitionId' when calling get3");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("bpmEngineId", bpmEngineId);
		uriVariables.put("processDefinitionId", processDefinitionId);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "application/xml" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<String> localReturnType = new ParameterizedTypeReference<String>() {
		};
		return apiClient.invokeAPI("/process-definition/{bpmEngineId}/{processDefinitionId}/xml", HttpMethod.GET,
				uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams,
				localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}
}
