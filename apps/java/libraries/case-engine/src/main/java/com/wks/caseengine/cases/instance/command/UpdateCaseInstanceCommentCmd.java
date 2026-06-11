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
import com.wks.caseengine.cases.instance.CaseComment;

public class UpdateCaseInstanceCommentCmd implements AuditableCommand<Void> {

	private final String businessKey;
	private final String commentId;
	private final String body;
	private CaseComment oldComment;

	public UpdateCaseInstanceCommentCmd(String businessKey, String commentId, String body) {
		this.businessKey = businessKey;
		this.commentId = commentId;
		this.body = body;
	}

	@Override
	public Void execute(CommandContext commandContext) {
		CaseInstance caseInstance;
		try {
			caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		if (caseInstance != null && caseInstance.getComments() != null) {
			this.oldComment = caseInstance.getComments().stream()
					.filter(c -> commentId.equals(c.getId()))
					.findFirst()
					.orElse(null);
		}

		try {
			commandContext.getCaseInstanceRepository().updateComment(businessKey, commentId, body);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		return null;
	}

	@Override
	public AuditEventType getAuditEventType() {
		return AuditEventType.COMMENT_UPDATED;
	}

	@Override
	public String getEntityId(CommandContext commandContext) {
		return businessKey;
	}

	@Override
	public String getAuditPayload(CommandContext commandContext, Void result) {
		Map<String, Object> payloadMap = new HashMap<>();
		if (oldComment != null) {
			payloadMap.put("commentId", oldComment.getId());
			Map<String, Object> diff = new HashMap<>();
			diff.put("before", oldComment.getBody());
			diff.put("after", body);
			payloadMap.put("body", diff);
		}
		return commandContext.getGsonBuilder().create().toJson(payloadMap);
	}

}
