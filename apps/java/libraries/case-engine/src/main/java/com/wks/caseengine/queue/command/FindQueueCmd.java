package com.wks.caseengine.queue.command;

import java.util.List;


import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.queue.Queue;

import lombok.AllArgsConstructor;
/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class FindQueueCmd implements Command<List<Queue>> {

	@Override
	public List<Queue> execute(CommandContext commandContext) {
		return commandContext.getQueueRepository().find();
	}

}
