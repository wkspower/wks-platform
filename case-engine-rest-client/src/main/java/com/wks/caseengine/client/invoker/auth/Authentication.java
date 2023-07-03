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
package com.wks.caseengine.client.invoker.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public interface Authentication {
	/**
	 * Apply authentication settings to header and / or query parameters.
	 *
	 * @param queryParams  The query parameters for the request
	 * @param headerParams The header parameters for the request
	 * @param cookieParams The cookie parameters for the request
	 */
	public void applyToParams(MultiValueMap<String, String> queryParams, HttpHeaders headerParams,
			MultiValueMap<String, String> cookieParams);
}
