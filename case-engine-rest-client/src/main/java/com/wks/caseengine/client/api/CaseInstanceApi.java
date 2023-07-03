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
import com.wks.caseengine.client.model.CaseDocument;
import com.wks.caseengine.client.model.CaseInstance;
import com.wks.caseengine.client.model.Comment;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class CaseInstanceApi {
	private ApiClient apiClient;

	public CaseInstanceApi() {
		this(new ApiClient());
	}

	public CaseInstanceApi(ApiClient apiClient) {
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
	 * @param businessKey (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void delete3(String businessKey) throws RestClientException {
		delete3WithHttpInfo(businessKey);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> delete3WithHttpInfo(String businessKey) throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'businessKey' is set
		if (businessKey == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'businessKey' when calling delete3");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("businessKey", businessKey);

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
		return apiClient.invokeAPI("/case/{businessKey}", HttpMethod.DELETE, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (required)
	 * @param commentId   (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void deleteComment(String businessKey, String commentId) throws RestClientException {
		deleteCommentWithHttpInfo(businessKey, commentId);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (required)
	 * @param commentId   (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> deleteCommentWithHttpInfo(String businessKey, String commentId)
			throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'businessKey' is set
		if (businessKey == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'businessKey' when calling deleteComment");
		}

		// verify the required parameter 'commentId' is set
		if (commentId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'commentId' when calling deleteComment");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("businessKey", businessKey);
		uriVariables.put("commentId", commentId);

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
		return apiClient.invokeAPI("/case/{businessKey}/comment/{commentId}", HttpMethod.DELETE, uriVariables,
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param status           (optional)
	 * @param caseDefinitionId (optional)
	 * @param before           (optional)
	 * @param after            (optional)
	 * @param sort             (optional)
	 * @param limit            (optional)
	 * @return Object
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public Object find6(String status, String caseDefinitionId, String before, String after, String sort, String limit)
			throws RestClientException {
		return find6WithHttpInfo(status, caseDefinitionId, before, after, sort, limit).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param status           (optional)
	 * @param caseDefinitionId (optional)
	 * @param before           (optional)
	 * @param after            (optional)
	 * @param sort             (optional)
	 * @param limit            (optional)
	 * @return ResponseEntity&lt;Object&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Object> find6WithHttpInfo(String status, String caseDefinitionId, String before, String after,
			String sort, String limit) throws RestClientException {
		Object localVarPostBody = null;

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "status", status));
		localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "caseDefinitionId", caseDefinitionId));
		localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "before", before));
		localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "after", after));
		localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "sort", sort));
		localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));

		final String[] localVarAccepts = { "*/*" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<Object> localReturnType = new ParameterizedTypeReference<Object>() {
		};
		return apiClient.invokeAPI("/case/", HttpMethod.GET, Collections.<String, Object>emptyMap(),
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (required)
	 * @return CaseInstance
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public CaseInstance get5(String businessKey) throws RestClientException {
		return get5WithHttpInfo(businessKey).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (required)
	 * @return ResponseEntity&lt;CaseInstance&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<CaseInstance> get5WithHttpInfo(String businessKey) throws RestClientException {
		Object localVarPostBody = null;

		// verify the required parameter 'businessKey' is set
		if (businessKey == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'businessKey' when calling get5");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("businessKey", businessKey);

		final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
		final HttpHeaders localVarHeaderParams = new HttpHeaders();
		final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
		final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

		final String[] localVarAccepts = { "*/*" };
		final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String[] localVarContentTypes = {};
		final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		String[] localVarAuthNames = new String[] {};

		ParameterizedTypeReference<CaseInstance> localReturnType = new ParameterizedTypeReference<CaseInstance>() {
		};
		return apiClient.invokeAPI("/case/{businessKey}", HttpMethod.GET, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param caseInstance (required)
	 * @return CaseInstance
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public CaseInstance save4(CaseInstance caseInstance) throws RestClientException {
		return save4WithHttpInfo(caseInstance).getBody();
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param caseInstance (required)
	 * @return ResponseEntity&lt;CaseInstance&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<CaseInstance> save4WithHttpInfo(CaseInstance caseInstance) throws RestClientException {
		Object localVarPostBody = caseInstance;

		// verify the required parameter 'caseInstance' is set
		if (caseInstance == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'caseInstance' when calling save4");
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

		ParameterizedTypeReference<CaseInstance> localReturnType = new ParameterizedTypeReference<CaseInstance>() {
		};
		return apiClient.invokeAPI("/case/", HttpMethod.POST, Collections.<String, Object>emptyMap(),
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (required)
	 * @param comment     (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void saveComment(String businessKey, Comment comment) throws RestClientException {
		saveCommentWithHttpInfo(businessKey, comment);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (required)
	 * @param comment     (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> saveCommentWithHttpInfo(String businessKey, Comment comment)
			throws RestClientException {
		Object localVarPostBody = comment;

		// verify the required parameter 'businessKey' is set
		if (businessKey == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'businessKey' when calling saveComment");
		}

		// verify the required parameter 'comment' is set
		if (comment == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'comment' when calling saveComment");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("businessKey", businessKey);

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
		return apiClient.invokeAPI("/case/{businessKey}/comment", HttpMethod.POST, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey  (required)
	 * @param caseDocument (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void saveDocument(String businessKey, CaseDocument caseDocument) throws RestClientException {
		saveDocumentWithHttpInfo(businessKey, caseDocument);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey  (required)
	 * @param caseDocument (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> saveDocumentWithHttpInfo(String businessKey, CaseDocument caseDocument)
			throws RestClientException {
		Object localVarPostBody = caseDocument;

		// verify the required parameter 'businessKey' is set
		if (businessKey == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'businessKey' when calling saveDocument");
		}

		// verify the required parameter 'caseDocument' is set
		if (caseDocument == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'caseDocument' when calling saveDocument");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("businessKey", businessKey);

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
		return apiClient.invokeAPI("/case/{businessKey}/document", HttpMethod.POST, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (required)
	 * @param commentId   (required)
	 * @param comment     (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void udpateComment(String businessKey, String commentId, Comment comment) throws RestClientException {
		udpateCommentWithHttpInfo(businessKey, commentId, comment);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey (required)
	 * @param commentId   (required)
	 * @param comment     (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> udpateCommentWithHttpInfo(String businessKey, String commentId, Comment comment)
			throws RestClientException {
		Object localVarPostBody = comment;

		// verify the required parameter 'businessKey' is set
		if (businessKey == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'businessKey' when calling udpateComment");
		}

		// verify the required parameter 'commentId' is set
		if (commentId == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'commentId' when calling udpateComment");
		}

		// verify the required parameter 'comment' is set
		if (comment == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'comment' when calling udpateComment");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("businessKey", businessKey);
		uriVariables.put("commentId", commentId);

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
		return apiClient.invokeAPI("/case/{businessKey}/comment/{commentId}", HttpMethod.PATCH, uriVariables,
				localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
				localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey  (required)
	 * @param caseInstance (required)
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public void update3(String businessKey, CaseInstance caseInstance) throws RestClientException {
		update3WithHttpInfo(businessKey, caseInstance);
	}

	/**
	 *
	 *
	 * <p>
	 * <b>200</b> - OK
	 *
	 * @param businessKey  (required)
	 * @param caseInstance (required)
	 * @return ResponseEntity&lt;Void&gt;
	 * @throws RestClientException if an error occurs while attempting to invoke the
	 *                             API
	 */
	public ResponseEntity<Void> update3WithHttpInfo(String businessKey, CaseInstance caseInstance)
			throws RestClientException {
		Object localVarPostBody = caseInstance;

		// verify the required parameter 'businessKey' is set
		if (businessKey == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'businessKey' when calling update3");
		}

		// verify the required parameter 'caseInstance' is set
		if (caseInstance == null) {
			throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
					"Missing the required parameter 'caseInstance' when calling update3");
		}

		// create path and map variables
		final Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("businessKey", businessKey);

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
		return apiClient.invokeAPI("/case/{businessKey}", HttpMethod.PATCH, uriVariables, localVarQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localReturnType);
	}
}
