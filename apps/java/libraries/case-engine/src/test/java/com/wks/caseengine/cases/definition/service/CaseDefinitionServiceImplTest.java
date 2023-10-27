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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.wks.caseengine.cases.definition.command.FindCaseDefinitionCmd;
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
	void shouldReturnCaseDefinitionWhenFind() throws Exception {

		// Given
		CaseDefinition existingCaseDefinition = new CaseDefinition();

		when(commandExecutor.execute(new FindCaseDefinitionCmd(Mockito.any()))).thenReturn(Arrays.<CaseDefinition>asList(existingCaseDefinition));

		// When
		List<CaseDefinition> result = service.find(Optional.empty());

		// Then
		assertEquals(Arrays.<CaseDefinition>asList(existingCaseDefinition), result);
		verify(commandExecutor).execute(Mockito.any());
	}

	void shouldReturnCaseDefinitionWhenFindDeployed() throws Exception {

		// Given
		CaseDefinition existingCaseDefinition = new CaseDefinition();

		when(commandExecutor.execute(new FindCaseDefinitionCmd(Mockito.any()))).thenReturn(Arrays.<CaseDefinition>asList(existingCaseDefinition));

		// When
		List<CaseDefinition> result = service.find(Optional.of(true));

		// Then
		assertEquals(Arrays.<CaseDefinition>asList(existingCaseDefinition), result);
		verify(commandExecutor).execute(Mockito.any());
	}
	
	@Test
	void shouldReturnCaseDefinitionWhenFindNotDeployed() throws Exception {

		// Given
		CaseDefinition existingCaseDefinition = new CaseDefinition();

		when(commandExecutor.execute(new FindCaseDefinitionCmd(Mockito.any()))).thenReturn(Arrays.<CaseDefinition>asList(existingCaseDefinition));

		// When
		List<CaseDefinition> result = service.find(Optional.of(false));

		// Then
		assertEquals(Arrays.<CaseDefinition>asList(existingCaseDefinition), result);
		verify(commandExecutor).execute(Mockito.any());
	}
	
	@Test
	void shouldReturnCaseDefinitionWhenGet() throws Exception {

//		// Given
//		CaseDefinition existingCaseDefinition = new CaseDefinition();
//		existingCaseDefinition.setId("someID");
//
//		when(commandExecutor.execute(new GetCaseDefinitionCmd(Mockito.any()))).thenReturn(Arrays.<CaseDefinition>asList(existingCaseDefinition));
//
//		// When
//		List<CaseDefinition> result = service.get(Optional.of(false));
//
//		// Then
//		assertEquals(Arrays.<CaseDefinition>asList(existingCaseDefinition), result);
//		verify(commandExecutor).execute(Mockito.any());
	}
}
