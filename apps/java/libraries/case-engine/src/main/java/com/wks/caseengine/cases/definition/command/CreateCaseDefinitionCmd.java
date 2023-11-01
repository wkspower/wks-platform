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

import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
@Setter
public class CreateCaseDefinitionCmd implements Command<CaseDefinition> {

	private CaseDefinition caseDefinition;

	@Override
	public CaseDefinition execute(final CommandContext commandContext) {
		commandContext.getCaseDefRepository().save(caseDefinition);
		return caseDefinition;
	}

}