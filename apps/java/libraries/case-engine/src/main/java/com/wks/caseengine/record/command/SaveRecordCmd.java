package com.wks.caseengine.record.command;

import com.google.gson.JsonObject;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SaveRecordCmd implements Command<Void> {

	private String recordTypeId;
	private JsonObject record;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getRecordRepository().save(recordTypeId, record);
		return null;
	}

}
