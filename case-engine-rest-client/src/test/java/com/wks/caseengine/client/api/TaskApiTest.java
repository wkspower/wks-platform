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

import com.wks.caseengine.client.model.Task;

/**
 * API tests for TaskApi
 */
@Ignore
public class TaskApiTest {

	private final TaskApi api = new TaskApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void claimTest() {
		String taskId = null;
		String taskAssignee = null;
		api.claim(taskId, taskAssignee);

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
	public void completeTest() {
		String taskId = null;
		String body = null;
		api.complete(taskId, body);

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
	public void findTest() {
		String businessKey = null;
		List<Task> response = api.find(businessKey);

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
	public void unclaimTest() {
		String taskId = null;
		api.unclaim(taskId);

		// TODO: test validations
	}

}
