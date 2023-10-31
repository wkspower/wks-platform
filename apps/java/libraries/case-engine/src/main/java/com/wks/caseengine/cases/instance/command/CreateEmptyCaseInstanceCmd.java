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

import java.util.ArrayList;
import java.util.Comparator;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateEmptyCaseInstanceCmd implements Command<CaseInstance> {

	private String caseDefinitionId;

	@Override
	public CaseInstance execute(CommandContext commandContext) {

		CaseDefinition caseDefinition = commandContext.getCaseDefRepository().get(caseDefinitionId);

		if (caseDefinition == null) {
			throw new CaseDefinitionNotFoundException();
		}

		String businessKey = commandContext.getBusinessKeyCreator().generate();
		CaseInstance newCaseInstance = CaseInstance.builder().businessKey(businessKey)
				.stage(caseDefinition.getStages().stream().sorted(Comparator.comparing(CaseStage::getIndex)).findFirst()
						.get().getName())
				.attributes(new ArrayList<>()).caseDefinitionId(caseDefinition.getId()).build();

		commandContext.getCaseInstanceRepository().save(newCaseInstance);

		commandContext.getProcessInstanceService().create(caseDefinition.getStagesLifecycleProcessKey(),
				newCaseInstance.getBusinessKey(), newCaseInstance.getAttributes());

		return newCaseInstance;
	}

}
