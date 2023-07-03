/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
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
import com.wks.caseengine.client.model.CaseDefinition;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class CaseDefinitionApi {
	private ApiClient apiClient;

	public CaseDefinitionApi() {
		this(new ApiClient());
	}

	public CaseDefinitionApi(ApiClient apiClient) {
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
	 * @param caseDefId (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void delete4(String caseDefId) throws RestClientException {
		delete4WithHttpInfo(caseDefId);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param caseDefId (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> delete4WithHttpInfo(String caseDefId) throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'caseDefId' is set
		if (caseDefId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'caseDefId' when calling delete4");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("caseDefId", caseDefId);

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
		return apiClient.invokeAPI("/case-definition/{caseDefId}", HttpMethod.DELETE, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param deployed (optional)
	 * @return List&lt;CaseDefinition&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public List<CaseDefinition> find7(Boolean deployed) throws RestClientException {
		return find7WithHttpInfo(deployed).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param deployed (optional)
	 * @return ResponseEntity&lt;List&lt;CaseDefinition&gt;&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<List<CaseDefinition>> find7WithHttpInfo(Boolean deployed) throws RestClientException {
		Object localVarPostBody = null;

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "deployed", deployed));

		final String[] localVarAccepts = { "*/*" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<List<CaseDefinition>> localReturnType = new ParameterizedTypeReference<List<CaseDefinition>>() {
		};
		return apiClient.invokeAPI("/case-definition/", HttpMethod.GET, Collections.<String, Object>emptyMap(),
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param caseDefId (required)
	 * @return CaseDefinition
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public CaseDefinition get6(String caseDefId) throws RestClientException {
		return get6WithHttpInfo(caseDefId).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param caseDefId (required)
	 * @return ResponseEntity&lt;CaseDefinition&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<CaseDefinition> get6WithHttpInfo(String caseDefId) throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'caseDefId' is set
		if (caseDefId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'caseDefId' when calling get6");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("caseDefId", caseDefId);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "*/*" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<CaseDefinition> localReturnType = new ParameterizedTypeReference<CaseDefinition>() {
		};
		return apiClient.invokeAPI("/case-definition/{caseDefId}", HttpMethod.GET, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param caseDefinition (required)
	 * @return CaseDefinition
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public CaseDefinition save5(CaseDefinition caseDefinition) throws RestClientException {
		return save5WithHttpInfo(caseDefinition).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param caseDefinition (required)
	 * @return ResponseEntity&lt;CaseDefinition&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<CaseDefinition> save5WithHttpInfo(CaseDefinition caseDefinition) throws RestClientException {
		Object localVarPostBody = caseDefinition;

		// verify the required parameter 'caseDefinition' is set
		if (caseDefinition == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'caseDefinition' when calling save5");
		}

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "*/*" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = { "application/json" };
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<CaseDefinition> localReturnType = new ParameterizedTypeReference<CaseDefinition>() {
		};
		return apiClient.invokeAPI("/case-definition/", HttpMethod.POST, Collections.<String, Object>emptyMap(),
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param caseDefId      (required)
	 * @param caseDefinition (required)
	 * @return CaseDefinition
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public CaseDefinition update4(String caseDefId, CaseDefinition caseDefinition) throws RestClientException {
		return update4WithHttpInfo(caseDefId, caseDefinition).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param caseDefId      (required)
	 * @param caseDefinition (required)
	 * @return ResponseEntity&lt;CaseDefinition&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<CaseDefinition> update4WithHttpInfo(String caseDefId, CaseDefinition caseDefinition)
			throws RestClientException {
		Object localVarPostBody = caseDefinition;

		// verify the required parameter 'caseDefId' is set
		if (caseDefId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'caseDefId' when calling update4");
		}

		// verify the required parameter 'caseDefinition' is set
		if (caseDefinition == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'caseDefinition' when calling update4");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("caseDefId", caseDefId);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "*/*" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = { "application/json" };
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<CaseDefinition> localReturnType = new ParameterizedTypeReference<CaseDefinition>() {
		};
		return apiClient.invokeAPI("/case-definition/{caseDefId}", HttpMethod.PATCH, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}
}
