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

public final class OpenPolicyRequest {

	private Map<String, Object> input;

	public OpenPolicyRequest(Map<String, Object> input) {
		this.input = input;
	}

	public Map<String, Object> getInput() {
		return input;
	}
}