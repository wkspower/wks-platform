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

import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.message.vm.AOPMessageVM;

@Component
public class VariableServiceImpl implements VariableService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public AOPMessageVM findVariables(final String processInstanceId) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			ProcessVariable[] variables = commandExecutor.execute(new FindVariableCommand(processInstanceId));
			response.setCode(200);
			response.setMessage("Process variables fetched successfully.");
			response.setData(variables);
		} catch (Exception e) {
			System.err.println("Error occurred while fetching process variables: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to fetch process variables: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}
}
