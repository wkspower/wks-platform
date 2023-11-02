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
public class UpdateFormCmd implements Command<Void> {

	private String formKey;
	private Form form;

	@Override
	public Void execute(CommandContext commandContext) {
		try {
			commandContext.getFormRepository().update(formKey, form);
		} catch (DatabaseRecordNotFoundException e) {
			throw new FormNotFoundException(e);
		}
		return null;
	}

}
