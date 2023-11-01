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
package com.wks.caseengine.form.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormRepository;

/**
 * @author victor.franca
 *
 */
@ExtendWith(MockitoExtension.class)
public class CreateFormCmdTest {

	@InjectMocks
	private CreateFormCmd createFormCmd;

	@InjectMocks
	private CommandContext commandContext;
	
	@Mock
	private FormRepository formRepository;

	@Test
	public void shouldCreateForm() {
		
		//Given
		Form form = new Form();
		form.setKey("FK_1");
		createFormCmd.setForm(form);

		//When
		createFormCmd.execute(commandContext);
		
		//Then
		verify(formRepository).save(form);

	}

}
