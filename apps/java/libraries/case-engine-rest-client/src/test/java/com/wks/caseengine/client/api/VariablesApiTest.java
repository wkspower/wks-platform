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

import org.junit.Ignore;
import org.junit.Test;

/**
 * API tests for VariablesApi
 */
@Ignore
public class VariablesApiTest {

	private final VariablesApi api = new VariablesApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void findVariablesTest() {
		String processInstanceId = null;
		String response = api.findVariables(processInstanceId);

		// TODO: test validations
	}

}
