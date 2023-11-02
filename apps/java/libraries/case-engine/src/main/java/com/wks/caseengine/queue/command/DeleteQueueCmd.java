package com.wks.caseengine.queue.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.queue.QueueNotFoundException;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class DeleteQueueCmd implements Command<Void> {

	private String queueId;

	@Override
	public Void execute(CommandContext commandContext) {
		try {
			commandContext.getQueueRepository().delete(queueId);
		} catch (DatabaseRecordNotFoundException e) {
			throw new QueueNotFoundException(e);
		}
		return null;
	}

}
