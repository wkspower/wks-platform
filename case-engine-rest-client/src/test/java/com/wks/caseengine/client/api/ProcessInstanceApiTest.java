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

import com.wks.caseengine.client.model.ActivityInstance;
import com.wks.caseengine.client.model.ProcessInstance;

/**
 * API tests for ProcessInstanceApi
 */
@Ignore
public class ProcessInstanceApiTest {

	private final ProcessInstanceApi api = new ProcessInstanceApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void find3Test() {
		String businessKey = null;
		List<ProcessInstance> response = api.find3(businessKey);

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
	public void getActivityInstancesTest() {
		String id = null;
		List<ActivityInstance> response = api.getActivityInstances(id);

		// TODO: test validations
	}

}
