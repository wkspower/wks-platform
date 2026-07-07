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
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.config.validation.ConfigDocType;
import com.wks.caseengine.config.validation.ConfigValidationService;
import com.wks.caseengine.form.command.CreateFormCmd;
import com.wks.caseengine.form.command.DeleteFormCmd;
import com.wks.caseengine.form.command.FindFormCmd;
import com.wks.caseengine.form.command.GetFormCmd;
import com.wks.caseengine.form.command.UpdateFormCmd;

@Component
public class FormServiceImpl implements FormService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Autowired
	private ConfigValidationService configValidationService;

	@Override
	@Transactional
	public void save(Form form) {
		configValidationService.validateOnWrite(ConfigDocType.FORM, form, form.getKey());
		commandExecutor.execute(new CreateFormCmd(form));
	}

	@Override
	public Form get(String formKey) {
		return commandExecutor.execute(new GetFormCmd(formKey));
	}

	@Override
	public List<Form> find() {
		return commandExecutor.execute(new FindFormCmd());
	}

	@Override
	@Transactional
	public void delete(String formKey) {
		commandExecutor.execute(new DeleteFormCmd(formKey));
	}

	@Override
	@Transactional
	public void update(final String formKey, final Form form) {
		configValidationService.validateOnWrite(ConfigDocType.FORM, form, formKey);
		commandExecutor.execute(new UpdateFormCmd(formKey, form));
	}

}
