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
import com.wks.caseengine.command.AuditableCommand;
import com.wks.caseengine.audit.AuditEventType;
import java.util.Map;
import java.util.HashMap;

public class PatchCaseInstanceCmd implements AuditableCommand<CaseInstance> {

	private final String businessKey;
	private final CaseInstance mergePatch;
	private CaseInstance oldCase;

	public PatchCaseInstanceCmd(String businessKey, CaseInstance mergePatch) {
		this.businessKey = businessKey;
		this.mergePatch = mergePatch;
	}

	@Override
	public CaseInstance execute(CommandContext commandContext) {
		CaseInstance target;
		try {
			target = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		if (target != null) {
			this.oldCase = CaseInstance.builder()
					.businessKey(target.getBusinessKey())
					.status(target.getStatus() != null ? target.getStatus().getCode() : null)
					.stage(target.getStage())
					.queueId(target.getQueueId())
					.build();

			if (mergePatch.getStatus() != null) {
				target.setStatus(mergePatch.getStatus());
			}
			if (mergePatch.getStage() != null) {
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
		}

		// TODO return the updated case instance from DB
		return target;
	}

	@Override
	public AuditEventType getAuditEventType() {
		return AuditEventType.CASE_UPDATED;
	}

	@Override
	public String getEntityId(CommandContext commandContext) {
		return businessKey;
	}

	@Override
	public String getAuditPayload(CommandContext commandContext, CaseInstance result) {
		Map<String, Object> payloadMap = new HashMap<>();
		if (oldCase != null && result != null) {
			if (oldCase.getStatus() != null && !oldCase.getStatus().equals(result.getStatus())) {
				payloadMap.put("status", createDiff(oldCase.getStatus().getCode(), result.getStatus() != null ? result.getStatus().getCode() : null));
			}
			if (oldCase.getStage() != null && !oldCase.getStage().equals(result.getStage())) {
				payloadMap.put("stage", createDiff(oldCase.getStage(), result.getStage()));
			}
			if (oldCase.getQueueId() != null && !oldCase.getQueueId().equals(result.getQueueId())) {
				payloadMap.put("queueId", createDiff(oldCase.getQueueId(), result.getQueueId()));
			}
		}
		return commandContext.getGsonBuilder().create().toJson(payloadMap);
	}

	private Map<String, Object> createDiff(Object before, Object after) {
		Map<String, Object> diff = new HashMap<>();
		diff.put("before", before);
		diff.put("after", after);
		return diff;
	}

}
