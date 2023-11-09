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
package com.wks.caseengine.tasks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.tasks.command.ClaimTaskCmd;
import com.wks.caseengine.tasks.command.CompleteTaskCmd;
import com.wks.caseengine.tasks.command.CreateTaskCmd;
import com.wks.caseengine.tasks.command.FindTaskCmd;
import com.wks.caseengine.tasks.command.UnclaimTaskCmd;

@Component
public class TaskServiceImpl implements TaskService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public void create(Task task){
		commandExecutor.execute(new CreateTaskCmd(task));
	}

	@Override
	public List<Task> find(final String processInstanceBusinessKey){
		return commandExecutor.execute(new FindTaskCmd(processInstanceBusinessKey));
	}

	@Override
	public void claim(final String taskId, final String taskAssignee){
		commandExecutor.execute(new ClaimTaskCmd(taskId, taskAssignee));

	}

	@Override
	public void unclaim(final String taskId){
		commandExecutor.execute(new UnclaimTaskCmd(taskId));
	}

	@Override
	public void complete(final String taskId, final JsonObject variables){
		commandExecutor.execute(new CompleteTaskCmd(taskId, variables));
	}

}
