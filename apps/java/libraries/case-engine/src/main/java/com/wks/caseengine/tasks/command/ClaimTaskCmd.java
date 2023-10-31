package com.wks.caseengine.tasks.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClaimTaskCmd implements Command<Void> {

	private String taskId;
	private String taskAssignee;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getBpmEngineClientFacade().claimTask(taskId, taskAssignee);
		return null;
	}

}
