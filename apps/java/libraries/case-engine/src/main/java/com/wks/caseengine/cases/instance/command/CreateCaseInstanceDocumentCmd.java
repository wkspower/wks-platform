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

import java.util.List;

import org.bson.types.ObjectId;

import com.wks.caseengine.cases.instance.CaseDocument;
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
public class CreateCaseInstanceDocumentCmd implements Command<CaseDocument> {

	private String businessKey;
	private CaseDocument document;

	@Override
	public CaseDocument execute(final CommandContext commandContext) {

		CaseInstance caseInstance;
		try {
			caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		// Server-assigned lifecycle metadata: a stable id (so the document can later
		// be addressed for status transitions), the received status, and the uploader
		// taken from the security context (never trusted from the client).
		document.setId(ObjectId.get().toString());
		document.setStatus(CaseDocument.STATUS_RECEIVED);
		commandContext.getSecurityContextTenantHolder().getUserId().ifPresent(document::setUploadedBy);

		applyVersioning(caseInstance, document);

		caseInstance.addDocument(document);

		try {
			commandContext.getCaseInstanceRepository().update(businessKey, caseInstance);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		return document;
	}

	/**
	 * Version the incoming document within its requirement's history. A document tied
	 * to a {@code requirementId} that already has a current document supersedes it:
	 * the previous current version is marked non-current and the new one takes the
	 * next version number, linking back via {@code supersedesId}. The first document
	 * for a requirement (and every free-form attachment) is version 1 and current.
	 */
	private void applyVersioning(final CaseInstance caseInstance, final CaseDocument incoming) {
		incoming.setCurrent(Boolean.TRUE);

		String requirementId = incoming.getRequirementId();
		List<CaseDocument> existing = caseInstance.getDocuments();
		CaseDocument previousCurrent = (requirementId == null || existing == null) ? null
				: existing.stream()
						.filter(d -> requirementId.equals(d.getRequirementId()))
						.filter(d -> !Boolean.FALSE.equals(d.getCurrent()))
						.reduce((first, second) -> second) // the latest current, if any
						.orElse(null);

		if (previousCurrent == null) {
			incoming.setVersion(1);
		} else {
			previousCurrent.setCurrent(Boolean.FALSE);
			int previousVersion = previousCurrent.getVersion() == null ? 1 : previousCurrent.getVersion();
			incoming.setVersion(previousVersion + 1);
			incoming.setSupersedesId(previousCurrent.getId());
		}
	}

}
