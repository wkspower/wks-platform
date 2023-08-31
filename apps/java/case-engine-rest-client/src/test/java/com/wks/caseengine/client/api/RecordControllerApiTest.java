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

import com.wks.caseengine.client.model.JsonObject;

/**
 * API tests for RecordControllerApi
 */
@Ignore
public class RecordControllerApiTest {

	private final RecordControllerApi api = new RecordControllerApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void deleteTest() {
		String recordTypeId = null;
		String id = null;
		api.delete(recordTypeId, id);

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
	public void find1Test() {
		String recordTypeId = null;
		List<JsonObject> response = api.find1(recordTypeId);

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
	public void getTest() {
		String recordTypeId = null;
		String id = null;
		JsonObject response = api.get(recordTypeId, id);

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
	public void saveTest() {
		String recordTypeId = null;
		JsonObject jsonObject = null;
		api.save(recordTypeId, jsonObject);

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
	public void updateTest() {
		String recordTypeId = null;
		String id = null;
		JsonObject jsonObject = null;
		api.update(recordTypeId, id, jsonObject);

		// TODO: test validations
	}

}
