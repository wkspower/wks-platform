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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseMilestone;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.CaseStageProcessDefinition;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.process.instance.CaseStageProcessStarter;

/**
 * Covers the real stage-transition path: a stage patch is how the portal and the
 * task-complete hooks move a case, so the stage's autoStart processes have to be
 * triggered from here — not from a helper nobody calls.
 */
@ExtendWith(MockitoExtension.class)
public class PatchCaseInstanceCmdTest {

	private static final String BUSINESS_KEY = "CASE-1";

	private static final String CASE_DEF_ID = "caseDef-1";

	@Mock
	private CommandContext commandContext;

	@Mock
	private CaseInstanceRepository caseInstanceRepository;

	@Mock
	private CaseDefinitionRepository caseDefinitionRepository;

	@Mock
	private CaseStageProcessStarter caseStageProcessStarter;

	private CaseInstance existingCase;

	@BeforeEach
	void setUp() throws Exception {
		existingCase = CaseInstance.builder().businessKey(BUSINESS_KEY).caseDefinitionId(CASE_DEF_ID).stage("Triage")
				.build();

		when(commandContext.getCaseInstanceRepository()).thenReturn(caseInstanceRepository);
		when(caseInstanceRepository.get(BUSINESS_KEY)).thenReturn(existingCase);
	}

	private CaseDefinition caseDefinition() {
		return CaseDefinition.builder()
				.stages(List.of(CaseStage.builder().id("2").index(1).name("Review")
						.processesDefinitions(List.of(CaseStageProcessDefinition.builder().definitionKey("reviewProc")
								.autoStart(true).build()))
						.build()))
				.build();
	}

	@Test
	void shouldStartTheAutoStartProcessesOfTheStageTheCaseMovesInto() throws Exception {
		CaseDefinition caseDefinition = caseDefinition();
		when(commandContext.getCaseDefRepository()).thenReturn(caseDefinitionRepository);
		when(caseDefinitionRepository.get(CASE_DEF_ID)).thenReturn(caseDefinition);
		when(commandContext.getCaseStageProcessStarter()).thenReturn(caseStageProcessStarter);

		CaseInstance patch = CaseInstance.builder().stage("Review").build();
		CaseInstance result = new PatchCaseInstanceCmd(BUSINESS_KEY, patch).execute(commandContext);

		assertEquals("Review", result.getStage());
		verify(caseInstanceRepository).update(BUSINESS_KEY, existingCase);
		verify(caseStageProcessStarter).startAutoStartProcesses(caseDefinition, "Review", BUSINESS_KEY);
	}

	/** Re-patching the stage the case is already in is not a transition. */
	@Test
	void shouldNotRestartProcessesWhenTheStageIsUnchanged() throws Exception {
		CaseInstance patch = CaseInstance.builder().stage("Triage").build();

		new PatchCaseInstanceCmd(BUSINESS_KEY, patch).execute(commandContext);

		verify(caseStageProcessStarter, never()).startAutoStartProcesses(any(), anyString(), anyString());
	}

	/** A status-only patch must not trigger anything stage-related. */
	@Test
	void shouldNotStartProcessesWhenOnlyTheStatusChanges() throws Exception {
		CaseInstance patch = CaseInstance.builder().status("Closed").build();

		new PatchCaseInstanceCmd(BUSINESS_KEY, patch).execute(commandContext);

		verify(caseStageProcessStarter, never()).startAutoStartProcesses(any(), anyString(), anyString());
	}

	private CaseDefinition caseDefinitionWithMilestone() {
		return CaseDefinition.builder()
				.stages(List.of(CaseStage.builder().id("1").index(0).name("Triage")
						.milestones(List.of(
								CaseMilestone.builder().id("person-erfasst").name("Person erfasst").build()))
						.build()))
				.build();
	}

	@Test
	void shouldRecordAnAchievedMilestoneWithItsNameFromTheDefinition() throws Exception {
		when(commandContext.getCaseDefRepository()).thenReturn(caseDefinitionRepository);
		when(caseDefinitionRepository.get(CASE_DEF_ID)).thenReturn(caseDefinitionWithMilestone());

		CaseInstance patch = CaseInstance.builder().achievedMilestone("person-erfasst").build();
		CaseInstance result = new PatchCaseInstanceCmd(BUSINESS_KEY, patch).execute(commandContext);

		assertEquals(1, result.getMilestones().size());
		assertEquals("person-erfasst", result.getMilestones().get(0).getId());
		assertEquals("Person erfasst", result.getMilestones().get(0).getName());
		assertNotNull(result.getMilestones().get(0).getAchievedAt());
		verify(caseInstanceRepository).update(BUSINESS_KEY, existingCase);
	}

	/**
	 * External tasks get retried, so the same milestone can be signalled twice. It
	 * must not be stamped twice — the achievement timestamp is a historical record.
	 */
	@Test
	void shouldIgnoreAMilestoneAlreadyAchieved() throws Exception {
		when(commandContext.getCaseDefRepository()).thenReturn(caseDefinitionRepository);
		when(caseDefinitionRepository.get(CASE_DEF_ID)).thenReturn(caseDefinitionWithMilestone());

		CaseInstance patch = CaseInstance.builder().achievedMilestone("person-erfasst").build();
		new PatchCaseInstanceCmd(BUSINESS_KEY, patch).execute(commandContext);
		String firstAchievedAt = existingCase.getMilestones().get(0).getAchievedAt();

		CaseInstance result = new PatchCaseInstanceCmd(BUSINESS_KEY, patch).execute(commandContext);

		assertEquals(1, result.getMilestones().size());
		assertEquals(firstAchievedAt, result.getMilestones().get(0).getAchievedAt());
	}

	/**
	 * A definition that no longer declares the milestone still gets the achievement
	 * recorded — the case really did reach something, and dropping it silently would
	 * hide drift between a definition and its generated processes.
	 */
	@Test
	void shouldRecordAnUndeclaredMilestoneByIdWithNoName() throws Exception {
		when(commandContext.getCaseDefRepository()).thenReturn(caseDefinitionRepository);
		when(caseDefinitionRepository.get(CASE_DEF_ID)).thenReturn(caseDefinition());

		CaseInstance patch = CaseInstance.builder().achievedMilestone("antrag-gestellt").build();
		CaseInstance result = new PatchCaseInstanceCmd(BUSINESS_KEY, patch).execute(commandContext);

		assertEquals(1, result.getMilestones().size());
		assertEquals("antrag-gestellt", result.getMilestones().get(0).getId());
		assertNull(result.getMilestones().get(0).getName());
	}

	/** Milestones and stage transitions are independent concerns. */
	@Test
	void shouldNotTouchMilestonesOnAPlainStagePatch() throws Exception {
		CaseDefinition caseDefinition = caseDefinition();
		when(commandContext.getCaseDefRepository()).thenReturn(caseDefinitionRepository);
		when(caseDefinitionRepository.get(CASE_DEF_ID)).thenReturn(caseDefinition);
		when(commandContext.getCaseStageProcessStarter()).thenReturn(caseStageProcessStarter);

		CaseInstance patch = CaseInstance.builder().stage("Review").build();
		CaseInstance result = new PatchCaseInstanceCmd(BUSINESS_KEY, patch).execute(commandContext);

		assertNull(result.getMilestones());
	}

	/** A milestone patch alone must not be mistaken for a stage transition. */
	@Test
	void shouldNotStartProcessesWhenOnlyAMilestoneIsAchieved() throws Exception {
		when(commandContext.getCaseDefRepository()).thenReturn(caseDefinitionRepository);
		when(caseDefinitionRepository.get(CASE_DEF_ID)).thenReturn(caseDefinitionWithMilestone());

		CaseInstance patch = CaseInstance.builder().achievedMilestone("person-erfasst").build();
		new PatchCaseInstanceCmd(BUSINESS_KEY, patch).execute(commandContext);

		verify(caseStageProcessStarter, never()).startAutoStartProcesses(any(), anyString(), anyString());
	}

}
