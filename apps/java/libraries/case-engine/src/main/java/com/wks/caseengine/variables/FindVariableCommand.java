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
package com.wks.caseengine.variables;

import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FindVariableCommand implements Command<ProcessVariable[]> {

	private String processInstanceId;

	@Override
	public ProcessVariable[] execute(CommandContext commandContext) {
		return commandContext.getBpmEngineClientFacade().findVariables(processInstanceId);
	}

}
