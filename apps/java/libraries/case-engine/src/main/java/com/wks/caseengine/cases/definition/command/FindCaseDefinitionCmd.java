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
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

/**
 * @author victor.franca
 *
 */
public class FindCaseDefinitionCmd implements Command<List<CaseDefinition>> {

	private Optional<FindCaseDefinitionFilter> caseDefinintionFilter;

	public FindCaseDefinitionCmd(Optional<FindCaseDefinitionFilter> filter) {
		this.caseDefinintionFilter = filter;
	}

	@Override
	public List<CaseDefinition> execute(CommandContext commandContext) {
		if (caseDefinintionFilter.isEmpty()) {
			return commandContext.getCaseDefRepository().find();
		} else {
			return commandContext.getCaseDefRepository().find(caseDefinintionFilter.get().getDeployed());
		}
	}

}
