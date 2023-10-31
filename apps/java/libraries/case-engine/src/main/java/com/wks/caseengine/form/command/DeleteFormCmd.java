package com.wks.caseengine.form.command;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DeleteFormCmd implements Command<Void> {

	private String formKey;

	@Override
	public Void execute(CommandContext commandContext) {
		commandContext.getFormRepository().delete(formKey);
		return null;
	}

}
