package com.wks.caseengine.audit.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.wks.caseengine.event.WksEvent;
import com.wks.caseengine.audit.service.AuditService;
import com.wks.caseengine.audit.AuditEventType;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(name = "wks.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditEventListener {

    private final AuditService auditService;

    public AuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onWksEvent(WksEvent event) {
        try {
            auditService.saveEvent(
                    AuditEventType.valueOf(event.getEventType()),
                    event.getEntityId(),
                    event.getEntityType(),
                    event.getPayload(),
                    event.getTenantId(),
                    event.getUserId()
            );
        } catch (Exception e) {
            log.error("Failed to write audit event for type {}", event.getEventType(), e);
        }
    }
}
