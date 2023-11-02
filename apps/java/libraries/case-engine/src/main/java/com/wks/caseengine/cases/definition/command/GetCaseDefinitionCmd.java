package com.wks.caseengine.cases.definition.command;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class GetCaseDefinitionCmd implements Command<CaseDefinition> {

	private String caseDefId;

	@Override
	public CaseDefinition execute(final CommandContext commandContext) {
		try {
			return commandContext.getCaseDefRepository().get(caseDefId);
		} catch (DatabaseRecordNotFoundException e) {
			throw new CaseDefinitionNotFoundException(e);
		}
	}

}
