package com.wks.caseengine.audit.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.audit.AuditEvent;
import com.wks.caseengine.audit.AuditEventType;
import com.wks.caseengine.audit.repository.AuditEventRepository;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.command.CommandContext;

@Component
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void auditCaseCreated(CaseInstance caseInstance, CommandContext commandContext) {
        String tenantId = commandContext.getSecurityContextTenantHolder().getTenantId().orElse("default");
        String userId = commandContext.getSecurityContextTenantHolder().getUserId().orElse("system");

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("businessKey", caseInstance.getBusinessKey());
        payloadMap.put("caseDefinitionId", caseInstance.getCaseDefinitionId());
        payloadMap.put("stage", caseInstance.getStage());
        payloadMap.put("status", caseInstance.getStatus());
        payloadMap.put("owner", caseInstance.getOwner() != null ? caseInstance.getOwner().getName() : null);

        String payload = commandContext.getGsonBuilder().create().toJson(payloadMap);

        AuditEvent event = AuditEvent.builder()
                .tenantId(tenantId)
                .userId(userId)
                .timestamp(Instant.now().toString())
                .eventType(AuditEventType.CASE_CREATED)
                .entityId(caseInstance.getBusinessKey())
                .entityType("CaseInstance")
                .payload(payload)
                .processed(false)
                .build();

        auditEventRepository.save(event);
    }

    public void auditCaseUpdated(CaseInstance oldCase, CaseInstance newCase, CommandContext commandContext) {
        String tenantId = commandContext.getSecurityContextTenantHolder().getTenantId().orElse("default");
        String userId = commandContext.getSecurityContextTenantHolder().getUserId().orElse("system");

        Map<String, Object> changes = new HashMap<>();

        if (oldCase.getStatus() != null && !oldCase.getStatus().equals(newCase.getStatus())) {
            changes.put("status", createDiff(oldCase.getStatus(), newCase.getStatus()));
        }
        if (oldCase.getStage() != null && !oldCase.getStage().equals(newCase.getStage())) {
            changes.put("stage", createDiff(oldCase.getStage(), newCase.getStage()));
        }
        if (oldCase.getQueueId() != null && !oldCase.getQueueId().equals(newCase.getQueueId())) {
            changes.put("queueId", createDiff(oldCase.getQueueId(), newCase.getQueueId()));
        }

        if (changes.isEmpty()) {
            return;
        }

        String payload = commandContext.getGsonBuilder().create().toJson(changes);

        AuditEvent event = AuditEvent.builder()
                .tenantId(tenantId)
                .userId(userId)
                .timestamp(Instant.now().toString())
                .eventType(AuditEventType.CASE_UPDATED)
                .entityId(newCase.getBusinessKey())
                .entityType("CaseInstance")
                .payload(payload)
                .processed(false)
                .build();

        auditEventRepository.save(event);
    }

    public void auditCaseDeleted(CaseInstance caseInstance, CommandContext commandContext) {
        String tenantId = commandContext.getSecurityContextTenantHolder().getTenantId().orElse("default");
        String userId = commandContext.getSecurityContextTenantHolder().getUserId().orElse("system");

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("businessKey", caseInstance.getBusinessKey());
        payloadMap.put("caseDefinitionId", caseInstance.getCaseDefinitionId());

        String payload = commandContext.getGsonBuilder().create().toJson(payloadMap);

        AuditEvent event = AuditEvent.builder()
                .tenantId(tenantId)
                .userId(userId)
                .timestamp(Instant.now().toString())
                .eventType(AuditEventType.CASE_DELETED)
                .entityId(caseInstance.getBusinessKey())
                .entityType("CaseInstance")
                .payload(payload)
                .processed(false)
                .build();

        auditEventRepository.save(event);
    }

    public void auditCommentAdded(String caseInstanceId, CaseComment comment, CommandContext commandContext) {
        String tenantId = commandContext.getSecurityContextTenantHolder().getTenantId().orElse("default");
        String userId = commandContext.getSecurityContextTenantHolder().getUserId().orElse("system");

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("commentId", comment.getId());
        payloadMap.put("body", comment.getBody());

        String payload = commandContext.getGsonBuilder().create().toJson(payloadMap);

        AuditEvent event = AuditEvent.builder()
                .tenantId(tenantId)
                .userId(userId)
                .timestamp(Instant.now().toString())
                .eventType(AuditEventType.COMMENT_ADDED)
                .entityId(caseInstanceId)
                .entityType("CaseInstance")
                .payload(payload)
                .processed(false)
                .build();

        auditEventRepository.save(event);
    }

    public void auditCommentUpdated(String caseInstanceId, CaseComment oldComment, String newBody, CommandContext commandContext) {
        String tenantId = commandContext.getSecurityContextTenantHolder().getTenantId().orElse("default");
        String userId = commandContext.getSecurityContextTenantHolder().getUserId().orElse("system");

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("commentId", oldComment.getId());
        payloadMap.put("body", createDiff(oldComment.getBody(), newBody));

        String payload = commandContext.getGsonBuilder().create().toJson(payloadMap);

        AuditEvent event = AuditEvent.builder()
                .tenantId(tenantId)
                .userId(userId)
                .timestamp(Instant.now().toString())
                .eventType(AuditEventType.COMMENT_UPDATED)
                .entityId(caseInstanceId)
                .entityType("CaseInstance")
                .payload(payload)
                .processed(false)
                .build();

        auditEventRepository.save(event);
    }

    public void auditCommentDeleted(String caseInstanceId, CaseComment comment, CommandContext commandContext) {
        String tenantId = commandContext.getSecurityContextTenantHolder().getTenantId().orElse("default");
        String userId = commandContext.getSecurityContextTenantHolder().getUserId().orElse("system");

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("commentId", comment.getId());
        payloadMap.put("body", comment.getBody());

        String payload = commandContext.getGsonBuilder().create().toJson(payloadMap);

        AuditEvent event = AuditEvent.builder()
                .tenantId(tenantId)
                .userId(userId)
                .timestamp(Instant.now().toString())
                .eventType(AuditEventType.COMMENT_DELETED)
                .entityId(caseInstanceId)
                .entityType("CaseInstance")
                .payload(payload)
                .processed(false)
                .build();

        auditEventRepository.save(event);
    }

    private Map<String, Object> createDiff(Object before, Object after) {
        Map<String, Object> diff = new HashMap<>();
        diff.put("before", before);
        diff.put("after", after);
        return diff;
    }

    public void saveEvent(AuditEventType eventType, String entityId, String entityType, String payload, CommandContext commandContext) {
        String tenantId = commandContext.getSecurityContextTenantHolder().getTenantId().orElse("default");
        String userId = commandContext.getSecurityContextTenantHolder().getUserId().orElse("system");

        AuditEvent event = AuditEvent.builder()
                .tenantId(tenantId)
                .userId(userId)
                .timestamp(Instant.now().toString())
                .eventType(eventType)
                .entityId(entityId)
                .entityType(entityType)
                .payload(payload)
                .processed(false)
                .build();

        auditEventRepository.save(event);
    }
}
