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
package com.wks.caseengine.cases.instance.service;

import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.pagination.PageResult;

public interface CaseInstanceService {

	PageResult<CaseInstance> find(CaseInstanceFilter filters);

	CaseInstance get(final String businessKey);

	CaseInstance startWithValues(final CaseInstance caseInstance);

	CaseInstance startEmpty(final String caseDefinitionId);
	
	void saveWithValues(final CaseInstance caseInstance);

	CaseInstance patch(final String businessKey, final CaseInstance caseInstance);

	void delete(final String businessKey);

	void saveDocument(final String businessKey, final CaseDocument document);

	void saveComment(final String businessKey, final CaseComment comment);

	void updateComment(final String businessKey, final String commentId, final String body);

	void deleteComment(final String businessKey, final String commentId);

}