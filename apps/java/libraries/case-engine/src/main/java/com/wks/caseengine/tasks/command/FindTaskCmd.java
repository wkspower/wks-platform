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
package com.wks.caseengine.tasks.command;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class FindTaskCmd implements Command<List<Task>> {

	private Optional<String> processInstanceBusinessKey;

	@Override
	public List<Task> execute(CommandContext commandContext) {
		return Arrays.asList(commandContext.getBpmEngineClientFacade().findTasks(processInstanceBusinessKey));
	}

}
