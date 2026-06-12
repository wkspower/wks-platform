package com.wks.caseengine.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	public <T> T execute(final Command<T> command) {
		T t = command.execute(commandContext);

		try {
			eventPublisher.publishEvent(new CommandExecutedEvent(command, t, commandContext));
		} catch (Exception e) {
			log.error("Failed to publish command execution event for {}", command.getClass().getSimpleName(), e);
		}

		if (commandContext.getSecurityContextTenantHolder().getUserId().isPresent()) {
			log.debug("Command {} executed by user {}", command.getClass().getSimpleName(),
					commandContext.getSecurityContextTenantHolder().getUserId().get());
		} else {
			log.debug("Command {} executed", command.getClass().getSimpleName());
		}

		return t;
	}
}