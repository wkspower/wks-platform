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

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionFilter;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class FindCaseDefinitionCmd implements Command<List<CaseDefinition>> {

	private Optional<CaseDefinitionFilter> caseDefinintionFilter;

	@Override
	public List<CaseDefinition> execute(final CommandContext commandContext) {
		if (caseDefinintionFilter.isEmpty()) {
			return commandContext.getCaseDefRepository().find();
		} else {
			return commandContext.getCaseDefRepository().find(caseDefinintionFilter.get().getDeployed());
		}
	}

}
