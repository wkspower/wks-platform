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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.command.CommandContext;

/**
 * @author victor.franca
 *
 */
@ExtendWith(MockitoExtension.class)
public class CreateCaseDefinitionCmdTest {

	@InjectMocks
	private CreateCaseDefinitionCmd createCaseDefinitionCmd;

	@InjectMocks
	private CommandContext commandContext;

	@Mock
	private CaseDefinitionRepository caseDefRepository;

	@Test
	public void shouldCreateCaseDefinition() {

		// Given
		CaseDefinition caseDefinition = new CaseDefinition();
		createCaseDefinitionCmd.setCaseDefinition(caseDefinition);

		// When
		CaseDefinition savedCaseDefinition = createCaseDefinitionCmd.execute(commandContext);

		// Then
		assertEquals(caseDefinition, savedCaseDefinition);
	}

}
