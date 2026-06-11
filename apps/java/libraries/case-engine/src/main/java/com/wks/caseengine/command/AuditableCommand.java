package com.wks.caseengine.command;

import com.wks.caseengine.audit.AuditEventType;

public interface AuditableCommand<T> extends Command<T> {

    AuditEventType getAuditEventType();

    String getEntityId(CommandContext commandContext);

    default String getEntityType() {
        return "CaseInstance";
    }

    String getAuditPayload(CommandContext commandContext, T result);
}
