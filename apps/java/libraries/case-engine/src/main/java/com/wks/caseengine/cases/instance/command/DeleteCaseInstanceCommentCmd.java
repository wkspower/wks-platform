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
package com.wks.caseengine.cases.instance.command;

import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class DeleteCaseInstanceCommentCmd implements Command<Void> {

	private String businessKey;
	private String commentId;

	@Override
	public Void execute(CommandContext commandContext) {
		CaseInstance caseInstance;
		try {
			caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e);
		}

		CaseComment comment = caseInstance.getComments().stream().filter(o -> commentId.equals(o.getId()))
				.reduce((a, b) -> {
					throw new IllegalStateException("Multiple elements: " + a + ", " + b);
				}).get();
		if (comment == null) {
			throw new CaseInstanceNotFoundException();
		}

		commandContext.getCaseInstanceRepository().deleteComment(businessKey, comment);

		return null;
	}

}
