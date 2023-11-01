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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.command.CreateCaseInstanceWithValuesCmd;
import com.wks.caseengine.cases.instance.command.CreateEmptyCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.DeleteCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.DeleteCaseInstanceCommentCmd;
import com.wks.caseengine.cases.instance.command.FindCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.GetCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.PatchCaseInstanceCmd;
import com.wks.caseengine.cases.instance.command.CreateCaseInstanceCommentCmd;
import com.wks.caseengine.cases.instance.command.CreateCaseInstanceDocumentCmd;
import com.wks.caseengine.cases.instance.command.UpdateCaseInstanceCommentCmd;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.pagination.PageResult;

@Component
public class CaseInstanceServiceImpl implements CaseInstanceService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public PageResult<CaseInstance> find(CaseInstanceFilter filters) {
		return commandExecutor.execute(new FindCaseInstanceCmd(filters));
	}

	@Override
	public CaseInstance get(final String businessKey) {
		return commandExecutor.execute(new GetCaseInstanceCmd(businessKey));
	}

	@Override
	public CaseInstance createWithValues(final CaseInstance caseInstance) {
		return commandExecutor.execute(new CreateCaseInstanceWithValuesCmd(caseInstance));
	}

	@Override
	public CaseInstance createEmpty(final String caseDefinitionId) {
		return commandExecutor.execute(new CreateEmptyCaseInstanceCmd(caseDefinitionId));

	}

	@Override
	public CaseInstance patch(final String businessKey, final CaseInstance mergePatch) {
		return commandExecutor.execute(new PatchCaseInstanceCmd(businessKey, mergePatch));
	}

	@Override
	public void delete(final String businessKey) throws CaseInstanceNotFoundException {
		commandExecutor.execute(new DeleteCaseInstanceCmd(businessKey));
	}

	@Override
	public void saveDocument(final String businessKey, final CaseDocument document) {
		commandExecutor.execute(new CreateCaseInstanceDocumentCmd(businessKey, document));
	}

	@Override
	public void saveComment(final String businessKey, final CaseComment comment) throws CaseInstanceNotFoundException {
		commandExecutor.execute(new CreateCaseInstanceCommentCmd(businessKey, comment));
	}

	@Override
	public void updateComment(final String businessKey, final String commentId, final String body)
			throws CaseInstanceNotFoundException {
		commandExecutor.execute(new UpdateCaseInstanceCommentCmd(businessKey, commentId, body));
	}

	@Override
	public void deleteComment(final String businessKey, final String commentId) throws CaseInstanceNotFoundException {
		commandExecutor.equals(new DeleteCaseInstanceCommentCmd(businessKey, commentId));
	}

}