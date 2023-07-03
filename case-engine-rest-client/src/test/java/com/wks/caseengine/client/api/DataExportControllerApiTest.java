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

import com.wks.caseengine.client.model.JsonObject;

/**
 * API tests for DataExportControllerApi
 */
@Ignore
public class DataExportControllerApiTest {

	private final DataExportControllerApi api = new DataExportControllerApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void exportTest() {
		JsonObject response = api.export();

		// TODO: test validations
	}

}
