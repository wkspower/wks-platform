package com.wks.caseengine.variables;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FindVariableCommand implements Command<String> {

	private String processInstanceId;

	@Override
	public String execute(CommandContext commandContext) {
		return commandContext.getBpmEngineClientFacade().findVariables(processInstanceId);
	}

}
