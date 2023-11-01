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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.command.CommandExecutor;

@Component
public class VariableServiceImpl implements VariableService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public String findVariables(final String processInstanceId) throws Exception {
		return commandExecutor.execute(new FindVariableCommand(processInstanceId));
	}

}
