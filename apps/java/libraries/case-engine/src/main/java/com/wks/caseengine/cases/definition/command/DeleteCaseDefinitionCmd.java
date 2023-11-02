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
package com.wks.caseengine.cases.definition.command;

import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class DeleteCaseDefinitionCmd implements Command<Void> {

	private String caseDefinitionId;

	@Override
	public Void execute(final CommandContext commandContext) {
		try {
			commandContext.getCaseDefRepository().delete(caseDefinitionId);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseDefinitionNotFoundException(e);
		}
		return null;
	}

}
