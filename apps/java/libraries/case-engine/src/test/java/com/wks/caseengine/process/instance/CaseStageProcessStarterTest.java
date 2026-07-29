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
package com.wks.caseengine.process.instance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.CaseStageProcessDefinition;

@ExtendWith(MockitoExtension.class)
public class CaseStageProcessStarterTest {

	@Mock
	private ProcessInstanceService processInstanceService;

	@InjectMocks
	private CaseStageProcessStarter starter;

	private CaseDefinition caseDefinitionWith(CaseStageProcessDefinition... processes) {
		return CaseDefinition.builder()
				.stages(List.of(
						CaseStage.builder().id("1").index(0).name("Triage").processesDefinitions(List.of(processes))
								.build(),
						CaseStage.builder().id("2").index(1).name("Review")
								.processesDefinitions(List.of(CaseStageProcessDefinition.builder()
										.definitionKey("otherStageProc").autoStart(true).build()))
								.build()))
				.build();
	}

	/**
	 * Regression: the autoStart flag was written by the case definition editor and
	 * read by nobody — a process marked autoStart simply never ran.
	 */
	@Test
	void shouldStartTheAutoStartProcessesOfTheStageBeingEntered() {
		CaseDefinition caseDefinition = caseDefinitionWith(
				CaseStageProcessDefinition.builder().definitionKey("autoProc").autoStart(true).build(),
				CaseStageProcessDefinition.builder().definitionKey("manualProc").autoStart(false).build());

		starter.startAutoStartProcesses(caseDefinition, "Triage", "CASE-1");

		verify(processInstanceService).start(eq("autoProc"), eq(Optional.of("CASE-1")), eq(List.of()));
		verify(processInstanceService, never()).start(eq("manualProc"), any(), anyList());
		// A different stage's autoStart process must not be dragged along.
		verify(processInstanceService, never()).start(eq("otherStageProc"), any(), anyList());
	}

	@Test
	void shouldDoNothingWhenTheStageHasNoAutoStartProcesses() {
		CaseDefinition caseDefinition = caseDefinitionWith(
				CaseStageProcessDefinition.builder().definitionKey("manualProc").autoStart(false).build());

		starter.startAutoStartProcesses(caseDefinition, "Triage", "CASE-1");

		verifyNoInteractions(processInstanceService);
	}

	/**
	 * One process failing to start must not take the rest of the stage's processes
	 * — or the transition that triggered them — down with it.
	 */
	@Test
	void shouldKeepStartingTheRemainingProcessesWhenOneFails() {
		CaseDefinition caseDefinition = caseDefinitionWith(
				CaseStageProcessDefinition.builder().definitionKey("brokenProc").autoStart(true).build(),
				CaseStageProcessDefinition.builder().definitionKey("healthyProc").autoStart(true).build());

		doThrow(new RuntimeException("engine down")).when(processInstanceService).start(eq("brokenProc"), any(),
				anyList());

		starter.startAutoStartProcesses(caseDefinition, "Triage", "CASE-1");

		verify(processInstanceService).start(eq("healthyProc"), eq(Optional.of("CASE-1")), eq(List.of()));
	}

	@Test
	void shouldIgnoreAnUnknownStage() {
		starter.startAutoStartProcesses(
				caseDefinitionWith(CaseStageProcessDefinition.builder().definitionKey("autoProc").autoStart(true)
						.build()),
				"NoSuchStage", "CASE-1");

		verify(processInstanceService, never()).start(anyString(), any(), anyList());
	}

}
