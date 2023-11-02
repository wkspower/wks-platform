package com.wks.caseengine.form.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormNotFoundException;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class GetFormCmd implements Command<Form> {

	private String formKey;

	@Override
	public Form execute(CommandContext commandContext) {
		try {
			return commandContext.getFormRepository().get(formKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new FormNotFoundException(e);
		}
	}

}
