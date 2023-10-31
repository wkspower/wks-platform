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

import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SaveCaseInstanceDocumentCmd implements Command<CaseDocument> {

	private String businessKey;
	private CaseDocument document;

	@Override
	public CaseDocument execute(final CommandContext commandContext) {

		CaseInstance caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);

		caseInstance.addDocument(document);

		commandContext.getCaseInstanceRepository().update(businessKey, caseInstance);

		return document;
	}

}
