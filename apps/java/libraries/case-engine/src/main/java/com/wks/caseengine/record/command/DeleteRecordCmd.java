package com.wks.caseengine.record.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DeleteRecordCmd implements Command<Void> {

	private String recordTypeId;
	private String recordId;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getRecordRepository().delete(recordTypeId, recordId);
		return null;
	}

}
