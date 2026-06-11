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

import java.util.Arrays;
import java.util.Date;

import org.bson.types.ObjectId;

import com.wks.caseengine.cases.instance.CaseComment;
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

public class CreateCaseInstanceCommentCmd implements AuditableCommand<CaseComment> {

	private final String businessKey;
	private final CaseComment comment;

	public CreateCaseInstanceCommentCmd(String businessKey, CaseComment comment) {
		this.businessKey = businessKey;
		this.comment = comment;
	}

	@Override
	public CaseComment execute(CommandContext commandContext) {
		CaseInstance caseInstance;
		try {
			caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		comment.setCreatedAt(new Date());

		comment.setId(ObjectId.get().toString());

		if (caseInstance != null) {
			if (caseInstance.getComments() == null) {
				caseInstance.setComments(Arrays.asList(comment));
			} else {
				caseInstance.getComments().add(comment);
			}

			try {
				commandContext.getCaseInstanceRepository().update(comment.getCaseId(), caseInstance);
			} catch (DatabaseRecordNotFoundException e) {
				throw new CaseInstanceNotFoundException(e.getMessage(), e);
			}
		}

		return comment;
	}

	@Override
	public AuditEventType getAuditEventType() {
		return AuditEventType.COMMENT_ADDED;
	}

	@Override
	public String getEntityId(CommandContext commandContext) {
		return businessKey;
	}

	@Override
	public String getAuditPayload(CommandContext commandContext, CaseComment result) {
		Map<String, Object> payloadMap = new HashMap<>();
		if (result != null) {
			payloadMap.put("commentId", result.getId());
			payloadMap.put("body", result.getBody());
		}
		return commandContext.getGsonBuilder().create().toJson(payloadMap);
	}

}
