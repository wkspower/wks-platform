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
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
import com.wks.caseengine.command.AuditableCommand;
import com.wks.caseengine.audit.AuditEventType;
import java.util.Map;
import java.util.HashMap;

public class SaveCaseInstanceWithValuesCmd implements AuditableCommand<Void> {

	private final CaseInstance caseInstance;
	private CaseInstance oldCase;
	private boolean isUpdate;

	public SaveCaseInstanceWithValuesCmd(CaseInstance caseInstance) {
		this.caseInstance = caseInstance;
	}

	@Override
	public Void execute(CommandContext commandContext) {
		try {
			CaseInstance target = commandContext.getCaseInstanceRepository().get(caseInstance.getBusinessKey());
			if (target != null) {
				// Create an in-memory snapshot of the audited fields
				this.oldCase = CaseInstance.builder()
						.businessKey(target.getBusinessKey())
						.status(target.getStatus() != null ? target.getStatus().getCode() : null)
						.stage(target.getStage())
						.queueId(target.getQueueId())
						.build();
				this.isUpdate = true;
			} else {
				this.isUpdate = false;
			}
		} catch (Exception e) {
			this.isUpdate = false;
		}

		commandContext.getCaseInstanceRepository().save(caseInstance);
		return null;
	}

	@Override
	public AuditEventType getAuditEventType() {
		return isUpdate ? AuditEventType.CASE_UPDATED : AuditEventType.CASE_CREATED;
	}

	@Override
	public String getEntityId(CommandContext commandContext) {
		return caseInstance.getBusinessKey();
	}

	@Override
	public String getAuditPayload(CommandContext commandContext, Void result) {
		Map<String, Object> payloadMap = new HashMap<>();
		if (isUpdate && oldCase != null) {
			if (oldCase.getStatus() != null && !oldCase.getStatus().equals(caseInstance.getStatus())) {
				payloadMap.put("status", createDiff(oldCase.getStatus().getCode(), caseInstance.getStatus() != null ? caseInstance.getStatus().getCode() : null));
			}
			if (oldCase.getStage() != null && !oldCase.getStage().equals(caseInstance.getStage())) {
				payloadMap.put("stage", createDiff(oldCase.getStage(), caseInstance.getStage()));
			}
			if (oldCase.getQueueId() != null && !oldCase.getQueueId().equals(caseInstance.getQueueId())) {
				payloadMap.put("queueId", createDiff(oldCase.getQueueId(), caseInstance.getQueueId()));
			}
		} else {
			payloadMap.put("businessKey", caseInstance.getBusinessKey());
			payloadMap.put("caseDefinitionId", caseInstance.getCaseDefinitionId());
			payloadMap.put("stage", caseInstance.getStage());
			payloadMap.put("status", caseInstance.getStatus() != null ? caseInstance.getStatus().getCode() : null);
			payloadMap.put("owner", caseInstance.getOwner() != null ? caseInstance.getOwner().getName() : null);
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
