package com.wks.caseengine.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommandExecutorImpl implements CommandExecutor {

	@Autowired
	private CommandContext commandContext;

	public <T> T execute(final Command<T> command) {
		T t = command.execute(commandContext);
		
		log.debug("Command {} executed", command.getClass().getSimpleName());
		
		return t;
	}
}