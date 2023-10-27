package com.wks.caseengine.command;

public interface CommandExecutor {

	<T> T execute(final Command<T> command);

}
