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

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
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
