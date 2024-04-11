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

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class SaveCaseEmailCmd implements Command<CaseEmail> {

	private CaseEmail caseEmail;

	@Override
	public CaseEmail execute(CommandContext commandContext) {
		String id = commandContext.getCaseEmailRepository().save(caseEmail);
		caseEmail.set_id(id);
		return caseEmail;
	}

}
