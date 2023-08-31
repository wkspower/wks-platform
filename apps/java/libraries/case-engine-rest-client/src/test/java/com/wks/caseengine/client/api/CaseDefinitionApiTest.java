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

import com.wks.caseengine.client.model.CaseDefinition;

/**
 * API tests for CaseDefinitionApi
 */
@Ignore
public class CaseDefinitionApiTest {

	private final CaseDefinitionApi api = new CaseDefinitionApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void delete4Test() {
		String caseDefId = null;
		api.delete4(caseDefId);

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
	public void find7Test() {
		Boolean deployed = null;
		List<CaseDefinition> response = api.find7(deployed);

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
	public void get6Test() {
		String caseDefId = null;
		CaseDefinition response = api.get6(caseDefId);

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
	public void save5Test() {
		CaseDefinition caseDefinition = null;
		CaseDefinition response = api.save5(caseDefinition);

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
	public void update4Test() {
		String caseDefId = null;
		CaseDefinition caseDefinition = null;
		CaseDefinition response = api.update4(caseDefId, caseDefinition);

		// TODO: test validations
	}

}
