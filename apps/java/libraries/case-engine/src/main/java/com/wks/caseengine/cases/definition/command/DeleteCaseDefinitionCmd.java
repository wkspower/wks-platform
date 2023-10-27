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
package com.wks.caseengine.cases.definition.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

public class DeleteCaseDefinitionCmd implements Command<Void> {

	private String caseDefinitionId;

	public DeleteCaseDefinitionCmd(final String caseDefinitionId) {
		this.caseDefinitionId = caseDefinitionId;
	}

	@Override
	public Void execute(final CommandContext commandContext) {
		commandContext.getCaseDefRepository().delete(caseDefinitionId);
		return null;
	}

}
