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

import java.util.Optional;

import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceCommentNotFoundException;
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

public class DeleteCaseInstanceCommentCmd implements AuditableCommand<Void> {

	private final String businessKey;
	private final String commentId;
	private CaseComment deletedComment;

	public DeleteCaseInstanceCommentCmd(String businessKey, String commentId) {
		this.businessKey = businessKey;
		this.commentId = commentId;
	}

	@Override
	public Void execute(CommandContext commandContext) {
		CaseInstance caseInstance;
		try {
			caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		if (caseInstance != null) {
			Optional<CaseComment> comment = caseInstance.getComments().stream().filter(o -> commentId.equals(o.getId()))
					.reduce((a, b) -> {
						throw new IllegalStateException("Multiple elements: " + a + ", " + b);
					});
			if (comment.isEmpty()) {
				throw new CaseInstanceCommentNotFoundException();
			}
			this.deletedComment = comment.get();

			try {
				commandContext.getCaseInstanceRepository().deleteComment(businessKey, comment.get());
			} catch (DatabaseRecordNotFoundException e) {
				throw new CaseInstanceNotFoundException(e.getMessage(), e);
			}
		}

		return null;
	}

	@Override
	public AuditEventType getAuditEventType() {
		return AuditEventType.COMMENT_DELETED;
	}

	@Override
	public String getEntityId(CommandContext commandContext) {
		return businessKey;
	}

	@Override
	public String getAuditPayload(CommandContext commandContext, Void result) {
		Map<String, Object> payloadMap = new HashMap<>();
		if (deletedComment != null) {
			payloadMap.put("commentId", deletedComment.getId());
			payloadMap.put("body", deletedComment.getBody());
		}
		return commandContext.getGsonBuilder().create().toJson(payloadMap);
	}

}
