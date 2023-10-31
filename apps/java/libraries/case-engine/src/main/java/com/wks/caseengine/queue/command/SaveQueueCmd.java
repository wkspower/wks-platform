package com.wks.caseengine.queue.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.queue.Queue;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SaveQueueCmd implements Command<Void> {

	private Queue queue;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getQueueRepository().save(queue);
		return null;
	}

}
