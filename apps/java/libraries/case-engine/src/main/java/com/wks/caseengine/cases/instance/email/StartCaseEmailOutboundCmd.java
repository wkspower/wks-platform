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

import java.util.Optional;

import com.google.gson.Gson;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.ProcessVariableType;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class StartCaseEmailOutboundCmd implements Command<Void> {

	private CaseEmail caseEmail;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getProcessInstanceService().start(commandContext.getEmailToCaseOutboundProcess(),
				Optional.empty(), Optional.of(generateCaseEmailProcessVariable(commandContext, caseEmail)));

		return null;
	}

	private ProcessVariable generateCaseEmailProcessVariable(CommandContext commandContext, CaseEmail caseEmail) {

		Gson gson = commandContext.getGsonBuilder().create();
		ProcessVariable caseInstanceProcessVariable = ProcessVariable.builder()
				.type(ProcessVariableType.JSON.getValue()).name("caseEmail")
				.value(gson.toJsonTree(caseEmail).toString()).build();

		return caseInstanceProcessVariable;
	}

}
