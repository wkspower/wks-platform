package com.wks.caseengine.form.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.form.Form;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetFormCmd implements Command<Form> {

	private String formKey;

	@Override
	public Form execute(CommandContext commandContext) {
		return commandContext.getFormRepository().get(formKey);
	}

}
