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
import java.util.stream.Collectors;

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
public class DeleteCaseInstanceCmd implements Command<Void> {

	private String businessKey;

	@Override
	public Void execute(CommandContext commandContext) {
		// TODO should not allow to delete. Close or archive instead
		// TODO Should ensure only one case is deleted - BusinessKey should be UNIQUE

		List<CaseInstance> caseInstanceList = commandContext.getCaseInstanceRepository().find().stream()
				.filter(o -> o.getBusinessKey().equals(businessKey)).collect(Collectors.toList());

		if (caseInstanceList.isEmpty()) {
			throw new CaseInstanceNotFoundException();
		}

		// TODO close/archive process in PostClose/Archive hook

		caseInstanceList.forEach(o -> {
			try {
				commandContext.getCaseInstanceRepository().delete(o.getBusinessKey());
			} catch (DatabaseRecordNotFoundException e) {
				throw new CaseInstanceNotFoundException(e.getMessage(), e);
			}

		});

		return null;
	}

}
