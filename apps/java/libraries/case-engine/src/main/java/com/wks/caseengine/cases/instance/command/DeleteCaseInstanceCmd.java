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

public class DeleteCaseInstanceCmd implements AuditableCommand<Void> {

	private final String businessKey;
	private CaseInstance caseInstance;

	public DeleteCaseInstanceCmd(String businessKey) {
		this.businessKey = businessKey;
	}

	@Override
	public Void execute(CommandContext commandContext) {
		// TODO should not allow to delete. Close or archive instead

		// TODO close/archive process in PostClose/Archive hook

		try {
			this.caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		try {
			commandContext.getCaseInstanceRepository().delete(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		return null;
	}

	@Override
	public AuditEventType getAuditEventType() {
		return AuditEventType.CASE_DELETED;
	}

	@Override
	public String getEntityId(CommandContext commandContext) {
		return businessKey;
	}

	@Override
	public String getAuditPayload(CommandContext commandContext, Void result) {
		Map<String, Object> payloadMap = new HashMap<>();
		if (caseInstance != null) {
			payloadMap.put("businessKey", caseInstance.getBusinessKey());
			payloadMap.put("caseDefinitionId", caseInstance.getCaseDefinitionId());
		}
		return commandContext.getGsonBuilder().create().toJson(payloadMap);
	}

}
