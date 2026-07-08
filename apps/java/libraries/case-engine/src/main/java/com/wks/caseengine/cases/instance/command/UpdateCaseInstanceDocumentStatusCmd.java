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
import java.util.Set;

import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceDocumentNotFoundException;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * Transitions a single case document's lifecycle status (verify / reject) and
 * stamps {@code validatedBy} from the security context. DB-agnostic: it loads the
 * case, mutates the document in the embedded list and saves the whole instance, so
 * it works identically on the Mongo and JPA persistence paths.
 */
@AllArgsConstructor
public class UpdateCaseInstanceDocumentStatusCmd implements Command<Void> {

	/** Statuses a validator may transition a document to via this command. */
	private static final Set<String> ALLOWED_TARGET_STATUSES =
			Set.of(CaseDocument.STATUS_VERIFIED, CaseDocument.STATUS_REJECTED);

	private String businessKey;
	private String documentId;
	private String status;

	@Override
	public Void execute(final CommandContext commandContext) {
		if (status == null || !ALLOWED_TARGET_STATUSES.contains(status)) {
			throw new IllegalArgumentException(
					"Unsupported document status '" + status + "'. Allowed: " + ALLOWED_TARGET_STATUSES);
		}

		CaseInstance caseInstance;
		try {
			caseInstance = commandContext.getCaseInstanceRepository().get(businessKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		List<CaseDocument> documents = caseInstance.getDocuments();
		CaseDocument document = documents == null ? null
				: documents.stream()
						.filter(d -> documentId != null && documentId.equals(d.getId()))
						.findFirst()
						.orElse(null);
		if (document == null) {
			throw new CaseInstanceDocumentNotFoundException(
					"Document '" + documentId + "' not found on case '" + businessKey + "'");
		}

		document.setStatus(status);
		commandContext.getSecurityContextTenantHolder().getUserId().ifPresent(document::setValidatedBy);

		try {
			commandContext.getCaseInstanceRepository().update(businessKey, caseInstance);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseInstanceNotFoundException(e.getMessage(), e);
		}

		return null;
	}

}
