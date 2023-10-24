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
package com.wks.api.security;

import java.util.Map;

import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;

public interface HandlerInputResolver {

	Map<String, Object> resolver(HttpServletRequest request, Authentication authentication);

}
