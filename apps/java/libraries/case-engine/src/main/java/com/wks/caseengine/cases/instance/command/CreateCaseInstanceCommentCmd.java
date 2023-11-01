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

import java.util.Arrays;
import java.util.Date;

import org.bson.types.ObjectId;

import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class CreateCaseInstanceCommentCmd implements Command<CaseComment> {

	private String businessKey;
	private CaseComment comment;

	@Override
	public CaseComment execute(CommandContext commandContext) {
		CaseInstance caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		if (caseInstance == null) {
			throw new CaseInstanceNotFoundException();
		}

		comment.setCreatedAt(new Date());

		comment.setId(ObjectId.get().toString());

		if (caseInstance.getComments() == null) {
			caseInstance.setComments(Arrays.asList(comment));
		} else {
			caseInstance.getComments().add(comment);
		}

		commandContext.getCaseInstanceRepository().update(comment.getCaseId(), caseInstance);

		return comment;
	}

}
