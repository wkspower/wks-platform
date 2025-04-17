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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.message.vm.AOPMessageVM;
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
	public AOPMessageVM create(Task task) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			commandExecutor.execute(new CreateTaskCmd(task));
			response.setCode(200);
			response.setMessage("Task created successfully.");
			response.setData(null); // You can set task if you want to return it
		} catch (Exception e) {
			System.err.println("Error while creating task: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to create task: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM find(final Optional<String> processInstanceBusinessKey) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			List<Task> taskList = commandExecutor.execute(new FindTaskCmd(processInstanceBusinessKey));
			response.setCode(200);
			response.setMessage("Tasks fetched successfully.");
			response.setData(taskList);
		} catch (Exception e) {
			System.err.println("Error while fetching tasks: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to fetch tasks: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM claim(final String taskId, final String taskAssignee) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			commandExecutor.execute(new ClaimTaskCmd(taskId, taskAssignee));
			response.setCode(200);
			response.setMessage("Task claimed successfully.");
			response.setData(null);
		} catch (Exception e) {
			System.err.println("Error while claiming task: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to claim task: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM unclaim(final String taskId) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			commandExecutor.execute(new UnclaimTaskCmd(taskId));
			response.setCode(200);
			response.setMessage("Task unclaimed successfully.");
			response.setData(null);
		} catch (Exception e) {
			System.err.println("Error while unclaiming task: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to unclaim task: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM complete(final String taskId, final List<ProcessVariable> variables) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			commandExecutor.execute(new CompleteTaskCmd(taskId, variables));
			response.setCode(200);
			response.setMessage("Task completed successfully.");
			response.setData(null);
		} catch (Exception e) {
			System.err.println("Error while completing task: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to complete task: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

}
