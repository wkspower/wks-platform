package com.wks.caseengine.command;

import com.wks.caseengine.cases.definition.CaseDefinition;

public class CreateCaseDefinitionCmd implements Command<CaseDefinition> {

	private CaseDefinition caseDefinition;

	public CreateCaseDefinitionCmd(final CaseDefinition caseDefinition) {
		this.caseDefinition = caseDefinition;
	}

	@Override
	public CaseDefinition execute(final CommandContext commandContext) {
		commandContext.getCaseDefRepository().save(caseDefinition);
		return caseDefinition;
	}

}