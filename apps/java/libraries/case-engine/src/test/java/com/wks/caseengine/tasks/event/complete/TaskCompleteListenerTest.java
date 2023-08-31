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
package com.wks.caseengine.tasks.event.complete;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.service.CaseDefinitionService;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.service.CaseInstanceService;

@ExtendWith(MockitoExtension.class)
public class TaskCompleteListenerTest {

	@Mock
	private CaseInstanceService caseInstanceService;

	@Mock
	private CaseDefinitionService caseDefinitionService;

	@InjectMocks
	private TaskCompleteListener taskCompleteListener;

	@Test
	public void testOnApplicationEvent() throws Exception {
		// Given
		TaskCompleteEvent event = new TaskCompleteEvent(
				new TaskCompleteEventObject("processDefKey1", "tskDefKey1", "businessKey1"));

		CaseInstance mockCaseInstance = mock(CaseInstance.class);
		when(mockCaseInstance.getCaseDefinitionId()).thenReturn("caseDefId1");

		CaseDefinition mockCaseDefinition = mock(CaseDefinition.class);
		when(mockCaseDefinition.getCaseHooks()).thenReturn(Collections.emptyList());

		when(caseInstanceService.get("businessKey1")).thenReturn(mockCaseInstance);
		when(caseDefinitionService.get("caseDefId1")).thenReturn(mockCaseDefinition);

		// When
		taskCompleteListener.onApplicationEvent(event);

		// Then
		verify(caseInstanceService).get("businessKey1");
		verify(caseDefinitionService).get("caseDefId1");
		verify(caseInstanceService).patch("businessKey1", mockCaseInstance);
	}

	@Test
	public void testOnApplicationEvent_WithException() throws Exception {
		// Given
		TaskCompleteEvent event = new TaskCompleteEvent(
				new TaskCompleteEventObject("processDefKey2", "tskDefKey2", "businessKey2"));
		when(caseInstanceService.get("businessKey2")).thenThrow(new RuntimeException("Test Exception"));

		// When
		try {
			taskCompleteListener.onApplicationEvent(event);
			fail("Expected an exception to be thrown");
		} catch (RuntimeException e) {
			// Then
			verify(caseInstanceService).get("businessKey2");
			verifyNoMoreInteractions(caseInstanceService, caseDefinitionService);
		}
	}

}
