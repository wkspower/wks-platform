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
public class UpdateQueueCmd implements Command<Void> {

	private String queueId;
	private Queue queue;

	@Override
	public Void execute(CommandContext commandContext) {
		try {
			commandContext.getQueueRepository().update(queueId, queue);
		} catch (DatabaseRecordNotFoundException e) {
			throw new QueueNotFoundException(e);
		}
		return null;
	}

}
