package com.wks.caseengine.rest.server.scheduler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wks.caseengine.audit.AuditEvent;
import com.wks.caseengine.audit.repository.AuditEventRepository;
import com.wks.caseengine.tenancy.TenantResolver;
import com.wks.caseengine.tenancy.TenantListProvider;
import com.wks.api.security.context.SecurityContextTenantHolder;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuditEventRelay {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private TenantResolver tenantResolver;

    @Autowired
    private SecurityContextTenantHolder tenantHolder;

    @Autowired(required = false)
    private TenantListProvider tenantListProvider;

    @Scheduled(fixedDelay = 5000)
    public void relayEvents() {
        if (!tenantResolver.isMultiTenant()) {
            relayForCurrentTenant();
        } else {
            List<String> tenantIds = Collections.emptyList();
            if (tenantListProvider != null) {
                tenantIds = tenantListProvider.getTenantIds();
            }
            
            if (tenantIds.isEmpty()) {
                log.debug("[AUDIT-RELAY] No active tenant found for audit processing.");
                return;
            }
            
            for (String tenantId : tenantIds) {
                try {
                    tenantHolder.setTenantId(tenantId);
                    tenantHolder.setUserId("audit-relay");
                    relayForCurrentTenant();
                } catch (Exception e) {
                    log.error("Error executing audit relay processor for tenant: " + tenantId, e);
                } finally {
                    tenantHolder.clear();
                }
            }
        }
    }

    private void relayForCurrentTenant() {
        try {
            List<AuditEvent> unprocessed = auditEventRepository.findUnprocessed();
            if (unprocessed != null && !unprocessed.isEmpty()) {
                log.info("Processing relay of {} pending audit events", unprocessed.size());
                
                for (AuditEvent event : unprocessed) {
                    // Forensic logging acting as SIEM / console log simulation
                    log.info("[AUDIT-RELAY] Event: {}, ID: {}, Entity: {} ({}), User: {}, Timestamp: {}, Payload: {}",
                            event.getEventType(),
                            event.getId(),
                            event.getEntityId(),
                            event.getEntityType(),
                            event.getUserId(),
                            event.getTimestamp(),
                            event.getPayload());
                }

                List<String> ids = unprocessed.stream()
                        .map(AuditEvent::getId)
                        .collect(Collectors.toList());
                        
                auditEventRepository.markProcessed(ids);
                log.debug("Audit events marked as processed: {}", ids);
            }
        } catch (Exception e) {
            log.error("Error executing audit relay processor", e);
        }
    }
}
