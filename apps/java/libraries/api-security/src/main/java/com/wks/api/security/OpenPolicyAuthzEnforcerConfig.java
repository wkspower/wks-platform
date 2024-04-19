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

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
public final class OpenPolicyAuthzEnforcerConfig {

	private String opaAuthURL;

	@Default
	private HandlerInputResolver handler = new BearerTokenHandlerInputResolver();

	@Default
	private boolean actuatorEnabled = false;

	@Default
	private boolean swaggerEnabled = false;

}