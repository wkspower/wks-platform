package com.wks.caseengine.record.command;

import java.util.List;

import com.google.gson.JsonObject;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FindRecordCmd implements Command<List<JsonObject>> {

	private String recordTypeId;

	@Override
	public List<JsonObject> execute(CommandContext commandContext) {
		return commandContext.getRecordRepository().find(recordTypeId);
	}

}
