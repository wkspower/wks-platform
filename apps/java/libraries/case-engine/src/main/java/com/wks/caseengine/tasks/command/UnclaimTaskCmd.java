package com.wks.caseengine.tasks.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UnclaimTaskCmd implements Command<Void> {

	private String taskId;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getBpmEngineClientFacade().unclaimTask(taskId);
		return null;
	}

}
