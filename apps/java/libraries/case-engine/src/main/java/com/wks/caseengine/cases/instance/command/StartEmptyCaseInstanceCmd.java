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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
@Setter
public class StartEmptyCaseInstanceCmd implements Command<CaseInstance> {

	private String caseDefinitionId;

	@Override
	// TODO replace by SAGA implementation using camunda case creation process
	public CaseInstance execute(CommandContext commandContext) {

		CaseDefinition caseDefinition;
		try {
			caseDefinition = commandContext.getCaseDefRepository().get(caseDefinitionId);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseDefinitionNotFoundException(e.getMessage(), e);
		}

		String businessKey = commandContext.getBusinessKeyCreator().generate();
		CaseInstance newCaseInstance = CaseInstance.builder().businessKey(businessKey)
				.stage(caseDefinition.getStages().stream().sorted(Comparator.comparing(CaseStage::getIndex)).findFirst()
						.get().getName())
				.attributes(new ArrayList<>()).caseDefinitionId(caseDefinition.getId())
				.caseDefinitionId(caseDefinitionId).build();

		commandContext.getCaseInstanceRepository().save(newCaseInstance);

		commandContext.getProcessInstanceService().create(caseDefinition.getStagesLifecycleProcessKey(),
				Optional.of(newCaseInstance.getBusinessKey()), Arrays.asList());

		return newCaseInstance;
	}

}
