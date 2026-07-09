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

		caseInstance.addDocument(document);

		try {
			commandContext.getCaseInstanceRepository().update(businessKey, caseInstance);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		return document;
	}

}
