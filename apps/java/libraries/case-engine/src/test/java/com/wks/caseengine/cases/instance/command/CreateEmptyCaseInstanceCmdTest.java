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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.cases.businesskey.GenericBusinessKeyGenerator;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.process.instance.ProcessInstanceService;


/**
 * @author victor.franca
 *
 */
@ExtendWith(MockitoExtension.class)
public class CreateEmptyCaseInstanceCmdTest {

	@InjectMocks
	private CreateEmptyCaseInstanceCmd createCaseInstanceCmd;

	@InjectMocks
	private CommandContext commandContext;

	@Mock
	private CaseInstanceRepository caseInstanceRepository;

	@Mock
	private CaseDefinitionRepository caseDefinitionRepository;

	@Mock
	private GenericBusinessKeyGenerator businessKeyCreator;
	
	@Mock
	private ProcessInstanceService processInstanceService;

	@Test
	public void shouldCreateCaseInstance() {

		// Given
		createCaseInstanceCmd.setCaseDefinitionId("CD_1");

		CaseDefinition caseDefinition = new CaseDefinition();
		caseDefinition.setStagesLifecycleProcessKey("Process1");
		caseDefinition.setStages(Arrays.<CaseStage>asList(CaseStage.builder().name("Stage 1").build()));

		// When
		when(caseDefinitionRepository.get("CD_1")).thenReturn(caseDefinition);
		when(businessKeyCreator.generate()).thenReturn("BK_1");
		CaseInstance savedCaseInstance = createCaseInstanceCmd.execute(commandContext);
		
		// Then
		assertEquals("BK_1", savedCaseInstance.getBusinessKey());
		assertEquals("CD_1", savedCaseInstance.getCaseDefinitionId());
		assertNull(savedCaseInstance.getCaseOwner());
		assertNull(savedCaseInstance.getCaseOwnerName());
		assertNull(savedCaseInstance.getComments());
		assertNull(savedCaseInstance.getDocuments());
		assertNull(savedCaseInstance.getQueueId());
		assertEquals("Stage 1", savedCaseInstance.getStage());
		assertNull(savedCaseInstance.getStatus());
		verify(processInstanceService).create(eq("Process1"), eq("BK_1"), Mockito.any());

	}

}
