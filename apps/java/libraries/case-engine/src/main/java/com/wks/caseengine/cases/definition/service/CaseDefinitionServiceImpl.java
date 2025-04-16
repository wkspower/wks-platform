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
package com.wks.caseengine.cases.definition.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionFilter;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.command.CreateCaseDefinitionCmd;
import com.wks.caseengine.cases.definition.command.DeleteCaseDefinitionCmd;
import com.wks.caseengine.cases.definition.command.FindCaseDefinitionCmd;
import com.wks.caseengine.cases.definition.command.GetCaseDefinitionCmd;
import com.wks.caseengine.cases.definition.command.UpdateCaseDefinitionCmd;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.message.vm.AOPMessageVM;

@Component
public class CaseDefinitionServiceImpl implements CaseDefinitionService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public AOPMessageVM find(final Optional<Boolean> deployed) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {

			List<CaseDefinition> list = commandExecutor.execute(
					new FindCaseDefinitionCmd(Optional.of(CaseDefinitionFilter.builder().deployed(deployed).build())));

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully...");
			aopMessageVM.setData(list);
		} catch (Exception e) {
			aopMessageVM.setCode(500);
			aopMessageVM.setMessage("Failed to fetch data: " + e.getMessage());
			aopMessageVM.setData(null);
		}
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM get(final String caseDefId) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			CaseDefinition caseDefinition = commandExecutor.execute(new GetCaseDefinitionCmd(caseDefId));
			response.setCode(200);
			response.setMessage("CaseDefination Fetched successfully...");
			response.setData(caseDefinition);
		} catch (CaseDefinitionNotFoundException e) {
			response.setCode(404);
			response.setMessage("CaseDefinition not found: " + e.getMessage());
			response.setData(null);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Unexpected error occurred");
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM create(final CaseDefinition caseDefinition) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			if (caseDefinition.getId() == null || caseDefinition.getId().isEmpty()) {
				// TODO error handling
				throw new IllegalArgumentException("No Case Definition ID provided");
			}
			CaseDefinition created = commandExecutor.execute(new CreateCaseDefinitionCmd(caseDefinition));
			response.setCode(200);
			response.setMessage("CaseDefinition created successfully");
			response.setData(created);
		} catch (IllegalArgumentException e) {
			response.setCode(400);
			response.setMessage("Invalid input: " + e.getMessage());
			response.setData(null);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Unexpected error occurred");
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM update(final String caseDefId, final CaseDefinition caseDefinition) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			CaseDefinition updated = commandExecutor.execute(new UpdateCaseDefinitionCmd(caseDefId, caseDefinition));
			response.setCode(200);
			response.setMessage("CaseDefinition updated successfully...");
			response.setData(updated);
		} catch (CaseDefinitionNotFoundException e) {
			response.setCode(404);
			response.setMessage("CaseDefinition not found: " + e.getMessage());
			response.setData(null);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Unexpected error while updating CaseDefinition: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public void delete(final String caseDefinitionId) {
		commandExecutor.execute(new DeleteCaseDefinitionCmd(caseDefinitionId));
	}

}
