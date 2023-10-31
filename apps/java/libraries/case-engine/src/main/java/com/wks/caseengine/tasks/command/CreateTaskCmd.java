package com.wks.caseengine.tasks.command;

import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateTaskCmd implements Command<Void> {

	private Task task;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getBpmEngineClientFacade().createTask(task);
		return null;
	}

}
