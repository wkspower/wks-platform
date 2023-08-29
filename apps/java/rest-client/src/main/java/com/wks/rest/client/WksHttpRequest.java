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
package com.wks.rest.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

/**
 * @author victor.franca
 *
 */
public interface WksHttpRequest {

	HttpMethod getHttpMethod();

	String getHttpRequestUrl();

	HttpEntity<?> getHttpEntity();

}
