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
package com.wks.caseengine.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author victor.franca
 *
 */
@Component
@Slf4j
public class CommandExecutorImpl implements CommandExecutor {

	@Autowired
	private CommandContext commandContext;

	public <T> T execute(final Command<T> command) {
		T t = command.execute(commandContext);
		
		log.debug("Command {} executed by user {}", command.getClass().getSimpleName(),
				commandContext.getSecurityContextTenantHolder().getUserId().get());
		
		return t;
	}
}