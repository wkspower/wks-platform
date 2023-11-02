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

import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
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
			throw new CaseInstanceNotFoundException(e);
		}

		if (mergePatch.getStatus() != null) {
			target.setStatus(mergePatch.getStatus());
		}
		if (mergePatch.getStage() != null) {
			target.setStage(mergePatch.getStage());
		}
		if (mergePatch.getQueueId() != null) {
			target.setQueueId(mergePatch.getQueueId());
		}

		commandContext.getCaseInstanceRepository().update(businessKey, target);

		// TODO return the updated case instance from DB
		return target;
	}

}
