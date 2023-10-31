package com.wks.caseengine.record.command;

import com.google.gson.JsonObject;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetRecordCmd implements Command<JsonObject> {

	private String recordTypeId;
	private String recordId;

	@Override
	public JsonObject execute(CommandContext commandContext) {
		return commandContext.getRecordRepository().get(recordTypeId, recordId);
	}

}
