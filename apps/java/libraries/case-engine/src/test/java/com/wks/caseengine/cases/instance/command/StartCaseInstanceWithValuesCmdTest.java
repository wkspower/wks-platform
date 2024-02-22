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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.wks.caseengine.cases.businesskey.GenericBusinessKeyGenerator;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.process.instance.ProcessInstanceService;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

/**
 * @author victor.franca
 *
 */
@ExtendWith(MockitoExtension.class)
public class StartCaseInstanceWithValuesCmdTest {

	@InjectMocks
	private StartCaseInstanceWithValuesCmd createCaseInstanceCmd;

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

	@Mock
	private GsonBuilder gsonBuilder;

	@Test
	public void shouldCreateCaseDefinition() throws DatabaseRecordNotFoundException {

		// Given
		CaseInstance caseInstanceToSave = new CaseInstance();
		caseInstanceToSave.setBusinessKey("BK_1");
		caseInstanceToSave.setCaseDefinitionId("CD_1");
		createCaseInstanceCmd.setCaseInstanceParam(caseInstanceToSave);

		CaseDefinition caseDefinition = new CaseDefinition();
		caseDefinition.setStagesLifecycleProcessKey("Process1");
		caseDefinition.setStages(Arrays.<CaseStage>asList(CaseStage.builder().name("Stage 1").build()));

		commandContext.setCaseCreationProcess("Process1");

		// When
		when(caseDefinitionRepository.get("CD_1")).thenReturn(caseDefinition);
		when(gsonBuilder.create()).thenReturn(new Gson());
		CaseInstance savedCaseInstance = createCaseInstanceCmd.execute(commandContext);

		// Then
		assertEquals("BK_1", savedCaseInstance.getBusinessKey());
		assertEquals("CD_1", savedCaseInstance.getCaseDefinitionId());
		assertEquals(caseInstanceToSave.getOwner(), savedCaseInstance.getOwner());
		assertEquals(caseInstanceToSave.getComments(), savedCaseInstance.getComments());
		assertEquals(caseInstanceToSave.getDocuments(), savedCaseInstance.getDocuments());
		assertEquals(caseInstanceToSave.getQueueId(), savedCaseInstance.getQueueId());
		assertEquals("Stage 1", savedCaseInstance.getStage());
		assertEquals(caseInstanceToSave.getStatus(), savedCaseInstance.getStatus());
		verify(processInstanceService).create(eq("Process1"), eq("BK_1"), any(JsonObject.class));
	}

}
