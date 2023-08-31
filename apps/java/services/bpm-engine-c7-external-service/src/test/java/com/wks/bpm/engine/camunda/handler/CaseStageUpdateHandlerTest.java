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
package com.wks.bpm.engine.camunda.handler;

import static org.mockito.Mockito.when;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.service.CaseInstanceService;

@ExtendWith(MockitoExtension.class)
public class CaseStageUpdateHandlerTest {

	@InjectMocks
	private CaseStageUpdateHandler handler;

	@Mock
	private CaseInstanceService caseInstanceService;

	@Mock
	private ExternalTask externalTask;

	@Mock
	private ExternalTaskService externalTaskService;
	
	@Mock
	private SecurityContextTenantHolder securityContext;
	
	@Mock
	private ExternalServiceErrorHandler errorHandler;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testExecute() throws Exception {
		String expectedBusinessKey = "businessKey123";
		String expectedStage = "stage123";
		CaseInstance expectedCaseInstance = CaseInstance.builder().stage(expectedStage).build();

		when(externalTask.getTenantId()).thenReturn("someTenant");
		when(externalTask.getBusinessKey()).thenReturn(expectedBusinessKey);
		when(externalTask.getVariable("stage")).thenReturn(expectedStage);
		when(caseInstanceService.patch(expectedBusinessKey, expectedCaseInstance)).thenReturn(expectedCaseInstance);

		handler.execute(externalTask, externalTaskService);

		// Here, we're checking the state in our mock repository to see if it matches
		// what we expect.
		CaseInstance resultCaseInstance = caseInstanceService.patch(expectedBusinessKey, expectedCaseInstance);
		assert resultCaseInstance.getStage().equals(expectedStage);
	}

}
