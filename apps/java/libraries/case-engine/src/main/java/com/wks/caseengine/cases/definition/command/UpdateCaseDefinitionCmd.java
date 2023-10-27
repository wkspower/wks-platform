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

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

public class UpdateCaseDefinitionCmd implements Command<CaseDefinition> {

	private String caseDefId;
	private CaseDefinition caseDefinition;

	public UpdateCaseDefinitionCmd(final String caseDefId, final CaseDefinition caseDefinition) {
		this.caseDefId = caseDefId;
		this.caseDefinition = caseDefinition;
	}

	@Override
	public CaseDefinition execute(final CommandContext commandContext) {
		commandContext.getCaseDefRepository().update(caseDefId, caseDefinition);
		return caseDefinition;
	}

}
