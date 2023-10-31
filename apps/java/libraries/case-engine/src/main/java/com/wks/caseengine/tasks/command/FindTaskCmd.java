package com.wks.caseengine.tasks.command;

import java.util.Arrays;
import java.util.List;

import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FindTaskCmd implements Command<List<Task>> {

	private String processInstanceBusinessKey;

	@Override
	public List<Task> execute(CommandContext commandContext) {
		return Arrays.asList(commandContext.getBpmEngineClientFacade().findTasks(processInstanceBusinessKey));
	}

}
