package com.wks.caseengine.queue.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.queue.Queue;
import com.wks.caseengine.queue.QueueNotFoundException;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

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
		try {
			return commandContext.getQueueRepository().get(queueId);
		} catch (DatabaseRecordNotFoundException e) {
			throw new QueueNotFoundException(e);
		}
	}

}
