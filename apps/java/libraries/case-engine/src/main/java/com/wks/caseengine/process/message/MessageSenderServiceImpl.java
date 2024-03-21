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
package com.wks.caseengine.process.message;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.ProcessVariable;

@Component
public class MessageSenderServiceImpl implements MessageSenderService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Override
	public void sendMessage(final ProcessMessage processMessage, final Optional<List<ProcessVariable>> variables) {
		processEngineClient.sendMessage(processMessage, variables);

	}

}
