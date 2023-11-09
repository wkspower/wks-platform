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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseAttributeType;
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
public class CreateCaseInstanceWithValuesCmd implements Command<CaseInstance> {

	private CaseInstance caseInstance;

	@Override
	public CaseInstance execute(CommandContext commandContext) {
		CaseDefinition caseDefinition;
		try {
			caseDefinition = commandContext.getCaseDefRepository().get(caseInstance.getCaseDefinitionId());
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseDefinitionNotFoundException(e.getMessage(), e);
		}

		caseInstance.addAttribute(
				new CaseAttribute("createdAt", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
						CaseAttributeType.STRING.getValue()));


		String businessKey = null;
		if (caseInstance.getBusinessKey() == null) {
			businessKey = commandContext.getBusinessKeyCreator().generate();
		} else {
			businessKey = caseInstance.getBusinessKey();
		}

		
		CaseInstance.CaseInstanceBuilder caseInstanceBuilder = CaseInstance.builder().businessKey(businessKey)
				.attributes(caseInstance.getAttributes()).caseDefinitionId(caseInstance.getCaseDefinitionId())
				.caseOwner(caseInstance.getCaseOwner()).caseOwnerName(caseInstance.getCaseOwnerName());

		Optional<CaseStage> firstStage = caseDefinition.getStages().stream().sorted(Comparator.comparing(CaseStage::getIndex))
				.findFirst();
		if(firstStage.isPresent()) {
			caseInstanceBuilder.stage(firstStage.get().getName());
		}
		
		CaseInstance newCaseInstance = caseInstanceBuilder.build();

		commandContext.getCaseInstanceRepository().save(newCaseInstance);

		commandContext.getProcessInstanceService().create(caseDefinition.getStagesLifecycleProcessKey(),
				newCaseInstance.getBusinessKey(), newCaseInstance.getAttributes());

		return newCaseInstance;
	}

}
