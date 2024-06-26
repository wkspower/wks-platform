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
package com.wks.caseengine.cases.instance.email;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class PatchCaseEmailCmd implements Command<Void> {

	private String id;
	private CaseEmail mergePatch;

	@Override
	public Void execute(CommandContext commandContext) {

		CaseEmail storedCaseEmail = null;
		try {
			storedCaseEmail = commandContext.getCaseEmailRepository().get(id);
			storedCaseEmail.setStatus(mergePatch.getStatus());
			storedCaseEmail.setReceivedDateTime(mergePatch.getReceivedDateTime());
			commandContext.getCaseEmailRepository().update(id, storedCaseEmail);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseEmailNotFoundException(e.getMessage(), e);
		}
		
		return null;
	}

}
