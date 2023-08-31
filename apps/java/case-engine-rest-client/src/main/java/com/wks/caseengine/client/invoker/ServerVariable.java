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
package com.wks.caseengine.client.invoker;

import java.util.HashSet;

/**
 * Representing a Server Variable for server URL template substitution.
 */
public class ServerVariable {
	public String description;
	public String defaultValue;
	public HashSet<String> enumValues = null;

	/**
	 * @param description  A description for the server variable.
	 * @param defaultValue The default value to use for substitution.
	 * @param enumValues   An enumeration of string values to be used if the
	 *                     substitution options are from a limited set.
	 */
	public ServerVariable(String description, String defaultValue, HashSet<String> enumValues) {
		this.description = description;
		this.defaultValue = defaultValue;
		this.enumValues = enumValues;
	}
}
