package com.wks.caseengine.queue.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.queue.Queue;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class GetQueueCmd implements Command<Queue> {

	private String queueId;

	@Override
	public Queue execute(CommandContext commandContext) {
		return commandContext.getQueueRepository().get(queueId);
	}

}
