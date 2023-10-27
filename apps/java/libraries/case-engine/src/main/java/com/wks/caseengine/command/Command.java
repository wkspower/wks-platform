package com.wks.caseengine.command;

public interface Command<T> {

	T execute(final CommandContext commandContext);

}
