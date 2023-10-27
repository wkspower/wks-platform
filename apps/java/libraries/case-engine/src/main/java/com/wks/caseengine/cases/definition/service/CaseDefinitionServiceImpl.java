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
import com.wks.caseengine.cases.definition.command.CreateCaseDefinitionCmd;
import com.wks.caseengine.cases.definition.command.FindCaseDefinitionCmd;
import com.wks.caseengine.cases.definition.command.FindCaseDefinitionFilter;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.command.CommandExecutor;

@Component
public class CaseDefinitionServiceImpl implements CaseDefinitionService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Autowired
	private CaseDefinitionRepository repository;

	@Override
	public List<CaseDefinition> find(final Optional<Boolean> deployed) throws Exception {
		return commandExecutor
				.execute(new FindCaseDefinitionCmd(
						Optional.of(FindCaseDefinitionFilter.builder().deployed(deployed).build())));
	}

	@Override
	public CaseDefinition get(final String caseDefId) throws Exception {
		return repository.get(caseDefId);
	}

	@Override
	public CaseDefinition create(final CaseDefinition caseDefinition) throws Exception {
		if (caseDefinition.getId().isEmpty()) {
			// TODO error handling
			throw new Exception("No Case Definition ID provided");
		}

		return commandExecutor.execute(new CreateCaseDefinitionCmd(caseDefinition));
	}

	@Override
	public CaseDefinition update(final String caseDefId, final CaseDefinition caseDefinition) throws Exception {
		repository.update(caseDefId, caseDefinition);
		return caseDefinition;
	}

	@Override
	public void delete(final String caseDefinitionId) throws CaseInstanceNotFoundException, Exception {
		repository.delete(caseDefinitionId);
	}

	public void setRepository(CaseDefinitionRepository repository) {
		this.repository = repository;
	}

}
