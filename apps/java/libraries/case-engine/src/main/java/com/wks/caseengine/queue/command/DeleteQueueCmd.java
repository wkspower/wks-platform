package com.wks.caseengine.queue.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DeleteQueueCmd implements Command<Void> {

	private String queueId;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getQueueRepository().delete(queueId);
		return null;
	}

}
