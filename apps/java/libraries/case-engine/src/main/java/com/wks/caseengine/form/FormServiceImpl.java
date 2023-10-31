/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.form.command.DeleteFormCmd;
import com.wks.caseengine.form.command.FindFormCmd;
import com.wks.caseengine.form.command.GetFormCmd;
import com.wks.caseengine.form.command.SaveFormCmd;
import com.wks.caseengine.form.command.UpdateFormCmd;

@Component
public class FormServiceImpl implements FormService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public void save(Form form) throws Exception {
		commandExecutor.execute(new SaveFormCmd(form));
	}

	@Override
	public Form get(String formKey) throws Exception {
		return commandExecutor.execute(new GetFormCmd(formKey));
	}

	@Override
	public List<Form> find() throws Exception {
		return commandExecutor.execute(new FindFormCmd());
	}

	@Override
	public void delete(String formKey) throws Exception {
		commandExecutor.execute(new DeleteFormCmd(formKey));
	}

	@Override
	public void update(final String formKey, final Form form) throws Exception {
		commandExecutor.execute(new UpdateFormCmd(formKey, form));
	}

}
