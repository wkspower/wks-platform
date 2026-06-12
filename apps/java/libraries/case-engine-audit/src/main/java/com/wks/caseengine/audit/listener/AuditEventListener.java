package com.wks.caseengine.audit.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.wks.caseengine.command.AuditableCommand;
import com.wks.caseengine.command.CommandExecutedEvent;
import com.wks.caseengine.audit.service.AuditService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(name = "wks.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditEventListener {

    @Autowired
    private AuditService auditService;

    @EventListener
    public void onCommandExecuted(CommandExecutedEvent event) {
        if (event.getCommand() instanceof AuditableCommand) {
            try {
                @SuppressWarnings("unchecked")
                AuditableCommand<Object> auditableCommand = (AuditableCommand<Object>) event.getCommand();
                auditService.saveEvent(
                        auditableCommand.getAuditEventType(),
                        auditableCommand.getEntityId(event.getCommandContext()),
                        auditableCommand.getEntityType(),
                        auditableCommand.getAuditPayload(event.getCommandContext(), event.getResult()),
                        event.getCommandContext()
                );
            } catch (Exception e) {
                log.error("Failed to write audit event for command {}", event.getCommand().getClass().getSimpleName(), e);
            }
        }
    }
}
