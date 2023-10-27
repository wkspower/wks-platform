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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.command.CreateCaseDefinitionCmd;
import com.wks.caseengine.cases.definition.command.FindCaseDefinitionCmd;
import com.wks.caseengine.cases.definition.command.GetCaseDefinitionCmd;
import com.wks.caseengine.cases.definition.command.UpdateCaseDefinitionCmd;
import com.wks.caseengine.command.CommandExecutor;

/**
 * @author victor.franca
 *
 */
@ExtendWith(MockitoExtension.class)
public class CaseDefinitionServiceImplTest {

	@Mock
	private CommandExecutor commandExecutor;

	@InjectMocks
	private CaseDefinitionServiceImpl service;

	@Test
	void shouldReturnEmptyListWhenFind() throws Exception {

		// Given
		when(commandExecutor.execute(new FindCaseDefinitionCmd(Mockito.any()))).thenReturn(Arrays.asList());

		// When
		List<CaseDefinition> result = service.find(Optional.empty());

		// Then
		assertEquals(Arrays.<CaseDefinition>asList(), result);
		verify(commandExecutor).execute(Mockito.any());
	}

	@Test
	void shouldReturnCaseDefinitionWhenGet() throws Exception {

		// Given
		CaseDefinition existingCaseDefinition = new CaseDefinition();
		existingCaseDefinition.setId("someID");

		when(commandExecutor.execute(new GetCaseDefinitionCmd(Mockito.any()))).thenReturn(existingCaseDefinition);

		// When
		CaseDefinition result = service.get("someId");

		// Then
		assertEquals(existingCaseDefinition, result);
		verify(commandExecutor).execute(Mockito.any());
	}

	@Test
	void shouldCreateCaseDefinition() {
		// Given
		CaseDefinition caseDefinition = new CaseDefinition();
		caseDefinition.setId("someID");

		when(commandExecutor.execute(new CreateCaseDefinitionCmd(Mockito.any()))).thenReturn(caseDefinition);

		// When
		CaseDefinition result = service.create(caseDefinition);

		// Then
		assertEquals(caseDefinition, result);
		verify(commandExecutor).execute(Mockito.any());

	}

	@Test
	void shouldThrowExceptionWhenCreatingCaseDefinitionWithNullOrEmptyId() {
		// Given
		CaseDefinition caseDefinition = new CaseDefinition();

		assertThatIllegalArgumentException().isThrownBy(() -> {
			service.create(caseDefinition);
		});
	}

	@Test
	void shouldUpdateCaseDefinition() {
		// Given
		CaseDefinition caseDefinition = new CaseDefinition();
		caseDefinition.setId("someID");
		caseDefinition.setName("someCaseName");

		when(commandExecutor.execute(new UpdateCaseDefinitionCmd("someID", Mockito.any()))).thenReturn(caseDefinition);

		// When
		CaseDefinition result = service.update("someID", caseDefinition);

		// Then
		assertEquals(caseDefinition, result);
	}

	@Test
	void shouldDeleteCaseDefinition() {
		// Given
		CaseDefinition caseDefinition = new CaseDefinition();
		caseDefinition.setId("someID");

		// When
		try {
			service.delete("someID");
		} catch (CaseDefinitionNotFoundException e) {
			fail("Unexpected CaseDefinitionNotFoundException.");
		}

		// Then
		verify(commandExecutor).execute(Mockito.any());
	}

}
