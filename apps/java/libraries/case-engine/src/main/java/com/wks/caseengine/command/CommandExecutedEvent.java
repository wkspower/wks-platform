package com.wks.caseengine.command;

public final class CommandExecutedEvent {
    private final Command<?> command;
    private final Object result;
    private final CommandContext commandContext;

    public CommandExecutedEvent(Command<?> command, Object result, CommandContext commandContext) {
        this.command = command;
        this.result = result;
        this.commandContext = commandContext;
    }

    public Command<?> getCommand() {
        return command;
    }

    public Object getResult() {
        return result;
    }

    public CommandContext getCommandContext() {
        return commandContext;
    }
}
