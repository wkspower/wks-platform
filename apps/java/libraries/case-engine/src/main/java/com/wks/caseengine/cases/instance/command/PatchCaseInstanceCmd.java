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

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseMilestone;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.CaseMilestoneState;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author victor.franca
 *
 */
@Slf4j
@AllArgsConstructor
public class PatchCaseInstanceCmd implements Command<CaseInstance> {

	private String businessKey;
	private CaseInstance mergePatch;

	@Override
	public CaseInstance execute(CommandContext commandContext) {
		CaseInstance target;
		try {
			target = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		if (mergePatch.getStatus() != null) {
			target.setStatus(mergePatch.getStatus());
		}

		// Remember whether this patch actually moves the case, so the new stage's
		// autoStart processes run once — and only on a real transition.
		String enteredStage = null;
		if (mergePatch.getStage() != null) {
			if (!mergePatch.getStage().equals(target.getStage())) {
				enteredStage = mergePatch.getStage();
			}
			target.setStage(mergePatch.getStage());
		}

		if (mergePatch.getQueueId() != null) {
			target.setQueueId(mergePatch.getQueueId());
		}

		if (mergePatch.getAchievedMilestone() != null) {
			achieveMilestone(commandContext, target, mergePatch.getAchievedMilestone());
		}

		try {
			commandContext.getCaseInstanceRepository().update(businessKey, target);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		if (enteredStage != null) {
			startStageProcesses(commandContext, target, enteredStage);
		}

		// TODO return the updated case instance from DB
		return target;
	}

	/**
	 * Records {@code milestoneId} as achieved on the case.
	 *
	 * <p>The display name is resolved from the case definition and denormalized
	 * onto the achievement, so renaming the milestone later does not rewrite
	 * history. A milestone the definition does not declare is still recorded —
	 * under its own id, with no name — because the case genuinely reached
	 * something and dropping the fact is worse than recording it thinly. That case
	 * is logged, since it usually means the definition and its generated processes
	 * have drifted apart.
	 */
	private void achieveMilestone(final CommandContext commandContext, final CaseInstance caseInstance,
			final String milestoneId) {

		if (caseInstance.hasMilestone(milestoneId)) {
			// Idempotent: an external task that gets retried must not stamp twice.
			log.debug("Milestone {} already achieved for case {} — ignoring", milestoneId,
					caseInstance.getBusinessKey());
			return;
		}

		String name = milestoneNameOf(commandContext, caseInstance, milestoneId);
		if (name == null) {
			log.warn("Milestone {} is not declared by case definition {} — recording it by id only", milestoneId,
					caseInstance.getCaseDefinitionId());
		}

		caseInstance.achieveMilestone(CaseMilestoneState.builder().id(milestoneId).name(name)
				.achievedAt(Instant.now().toString()).build());

		log.debug("Milestone {} achieved for case {}", milestoneId, caseInstance.getBusinessKey());
	}

	private String milestoneNameOf(final CommandContext commandContext, final CaseInstance caseInstance,
			final String milestoneId) {
		try {
			CaseDefinition caseDefinition = commandContext.getCaseDefRepository()
					.get(caseInstance.getCaseDefinitionId());

			if (caseDefinition.getStages() == null) {
				return null;
			}

			return caseDefinition.getStages().stream().map(CaseStage::getMilestones).filter(Objects::nonNull)
					.flatMap(List::stream).filter(milestone -> milestoneId.equals(milestone.getId()))
					.map(CaseMilestone::getName).findFirst().orElse(null);

		} catch (DatabaseRecordNotFoundException e) {
			log.error("Case definition {} not found — recording milestone {} by id only",
					caseInstance.getCaseDefinitionId(), milestoneId, e);
			return null;
		}
	}

	private void startStageProcesses(final CommandContext commandContext, final CaseInstance caseInstance,
			final String enteredStage) {
		try {
			CaseDefinition caseDefinition = commandContext.getCaseDefRepository()
					.get(caseInstance.getCaseDefinitionId());
			commandContext.getCaseStageProcessStarter().startAutoStartProcesses(caseDefinition, enteredStage,
					caseInstance.getBusinessKey());
		} catch (DatabaseRecordNotFoundException e) {
			// The stage moved and is persisted; a missing definition can't undo that.
			log.error("Case definition {} not found — no autoStart processes ran for case {} entering stage {}",
					caseInstance.getCaseDefinitionId(), caseInstance.getBusinessKey(), enteredStage, e);
		}
	}

}
