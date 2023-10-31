package com.wks.caseengine.tasks.command;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.tasks.event.complete.TaskCompleteEvent;
import com.wks.caseengine.tasks.event.complete.TaskCompleteEventObject;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CompleteTaskCmd implements Command<Void> {

	private String taskId;
	private JsonObject variables;

	@Override
	public Void execute(CommandContext commandContext) {
		Task task = commandContext.getBpmEngineClientFacade().getTask(taskId);
		commandContext.getBpmEngineClientFacade().complete(taskId, variables);

		commandContext.getApplicationEventPublisher()
				.publishEvent(new TaskCompleteEvent(new TaskCompleteEventObject(task.getProcessDefinitionId(),
						task.getTaskDefinitionKey(), task.getCaseInstanceId())));
		return null;

	}

}
