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

public final class OpenPolicyResponse {

	private boolean result;

	public boolean getResult() {
		return this.result;
	}

	@Override
	public String toString() {
		return String.format("result: %s", result);
	}

}