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
package com.wks.caseengine.cases.instance.repository;

import com.wks.caseengine.cases.instance.CaseFilter;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.Comment;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.repository.Repository;

public interface CaseInstanceRepository extends Repository<CaseInstance> {

	PageResult<CaseInstance> find(CaseFilter filters) throws Exception;

	void deleteComment(final String businessKey, final Comment comment);

	void updateComment(final String businessKey, final String commentId, final String body);

}
