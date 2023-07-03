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

import com.wks.caseengine.client.model.CaseDocument;
import com.wks.caseengine.client.model.CaseInstance;
import com.wks.caseengine.client.model.Comment;

/**
 * API tests for CaseInstanceApi
 */
@Ignore
public class CaseInstanceApiTest {

	private final CaseInstanceApi api = new CaseInstanceApi();

	/**
	 *
	 *
	 *
	 *
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void delete3Test() {
		String businessKey = null;
		api.delete3(businessKey);

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
	public void deleteCommentTest() {
		String businessKey = null;
		String commentId = null;
		api.deleteComment(businessKey, commentId);

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
	public void find6Test() {
		String status = null;
		String caseDefinitionId = null;
		String before = null;
		String after = null;
		String sort = null;
		String limit = null;
		Object response = api.find6(status, caseDefinitionId, before, after, sort, limit);

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
	public void get5Test() {
		String businessKey = null;
		CaseInstance response = api.get5(businessKey);

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
	public void save4Test() {
		CaseInstance caseInstance = null;
		CaseInstance response = api.save4(caseInstance);

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
	public void saveCommentTest() {
		String businessKey = null;
		Comment comment = null;
		api.saveComment(businessKey, comment);

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
	public void saveDocumentTest() {
		String businessKey = null;
		CaseDocument caseDocument = null;
		api.saveDocument(businessKey, caseDocument);

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
	public void udpateCommentTest() {
		String businessKey = null;
		String commentId = null;
		Comment comment = null;
		api.udpateComment(businessKey, commentId, comment);

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
	public void update3Test() {
		String businessKey = null;
		CaseInstance caseInstance = null;
		api.update3(businessKey, caseInstance);

		// TODO: test validations
	}

}
