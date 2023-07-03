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

import com.wks.caseengine.client.model.Form;

/**
 * API tests for FormApi
 */
@Ignore
public class FormApiTest {

	private final FormApi api = new FormApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void delete2Test() {
		String formKey = null;
		api.delete2(formKey);

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
	public void find5Test() {
		List<Form> response = api.find5();

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
	public void get4Test() {
		String formKey = null;
		Form response = api.get4(formKey);

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
	public void save3Test() {
		Form form = null;
		api.save3(form);

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
	public void update2Test() {
		String formKey = null;
		Form form = null;
		api.update2(formKey, form);

		// TODO: test validations
	}

}
