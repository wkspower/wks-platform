package com.wks.caseengine.form.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.form.FormNotFoundException;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class DeleteFormCmd implements Command<Void> {

	private String formKey;

	@Override
	public Void execute(CommandContext commandContext) {
		try {
			commandContext.getFormRepository().delete(formKey);
		} catch (DatabaseRecordNotFoundException e) {
			throw new FormNotFoundException(e);
		}
		return null;
	}

}
