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

import com.wks.caseengine.client.model.RecordType;

/**
 * API tests for RecordTypeControllerApi
 */
@Ignore
public class RecordTypeControllerApiTest {

	private final RecordTypeControllerApi api = new RecordTypeControllerApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void delete1Test() {
		String id = null;
		api.delete1(id);

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
	public void find2Test() {
		List<RecordType> response = api.find2();

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
	public void get1Test() {
		String id = null;
		RecordType response = api.get1(id);

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
	public void save1Test() {
		RecordType recordType = null;
		api.save1(recordType);

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
	public void update1Test() {
		String id = null;
		RecordType recordType = null;
		api.update1(id, recordType);

		// TODO: test validations
	}

}
