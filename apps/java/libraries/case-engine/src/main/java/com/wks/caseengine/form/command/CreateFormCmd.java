package com.wks.caseengine.form.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.form.Form;

import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
@Setter
public class CreateFormCmd implements Command<Void> {

	private Form form;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getFormRepository().save(form);
		return null;
	}

}
