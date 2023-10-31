package com.wks.caseengine.cases.definition.command;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetCaseDefinitionCmd implements Command<CaseDefinition> {

	private String caseDefId;

	@Override
	public CaseDefinition execute(final CommandContext commandContext) {
		return commandContext.getCaseDefRepository().get(caseDefId);
	}

}
