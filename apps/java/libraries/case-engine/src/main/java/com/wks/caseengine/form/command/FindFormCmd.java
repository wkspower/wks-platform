package com.wks.caseengine.form.command;

import java.util.List;

import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.form.Form;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class FindFormCmd implements Command<List<Form>> {

	@Override
	public List<Form> execute(CommandContext commandContext) {
		return commandContext.getFormRepository().find();
	}

}
