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

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.wks.caseengine.client.model.ProcessDefinition;

/**
 * API tests for ProcessDefinitionApi
 */
@Ignore
public class ProcessDefinitionApiTest {

	private final ProcessDefinitionApi api = new ProcessDefinitionApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void find4Test() {
		List<ProcessDefinition> response = api.find4();

		// TODO: test validations
	}

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void get2Test() {
		String processDefinitionId = null;
		String response = api.get2(processDefinitionId);

		// TODO: test validations
	}

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void get3Test() {
		String bpmEngineId = null;
		String processDefinitionId = null;
		String response = api.get3(bpmEngineId, processDefinitionId);

		// TODO: test validations
	}

}
