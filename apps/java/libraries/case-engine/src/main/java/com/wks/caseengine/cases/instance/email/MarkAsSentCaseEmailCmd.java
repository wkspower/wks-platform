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
package com.wks.caseengine.cases.instance.email;

import java.util.Arrays;
import java.util.Optional;

import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class MarkAsSentCaseEmailCmd implements Command<Void> {

	private String id;

	@Override
	public Void execute(CommandContext commandContext) {

		ProcessVariable caseEmailIdCorrelateKey = ProcessVariable.builder().name("caseEmailId").value(id).build();

		commandContext.getBpmEngineClientFacade().sendMessage(
				ProcessMessage.builder().messageCode("emailSentConfirmation").build(),
				Optional.of(Arrays.asList(caseEmailIdCorrelateKey)));

		return null;
	}

}
