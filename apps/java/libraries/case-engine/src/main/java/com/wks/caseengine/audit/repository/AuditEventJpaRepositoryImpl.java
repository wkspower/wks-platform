package com.wks.caseengine.audit.repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.audit.AuditEvent;
import com.wks.caseengine.jpa.entity.AuditEventEntity;

@Component
@Transactional
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class AuditEventJpaRepositoryImpl implements AuditEventRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(AuditEvent auditEvent) {
        AuditEventEntity entity = toEntity(auditEvent);
        entityManager.persist(entity);
        auditEvent.setId(entity.getUid().toString());
    }

    @Override
    public List<AuditEvent> findByCaseInstanceId(String caseInstanceId) {
        TypedQuery<AuditEventEntity> query = entityManager.createQuery(
            "SELECT a FROM AuditEventEntity a WHERE a.entityId = :caseInstanceId AND a.entityType = 'CaseInstance' ORDER BY a.timestamp DESC",
            AuditEventEntity.class
        );
        query.setParameter("caseInstanceId", caseInstanceId);
        return query.getResultList().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditEvent> findUnprocessed() {
        TypedQuery<AuditEventEntity> query = entityManager.createQuery(
            "SELECT a FROM AuditEventEntity a WHERE a.processed = false ORDER BY a.timestamp ASC",
            AuditEventEntity.class
        );
        return query.getResultList().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void markProcessed(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<UUID> uuids = ids.stream().map(UUID::fromString).collect(Collectors.toList());
        entityManager.createQuery("UPDATE AuditEventEntity a SET a.processed = true WHERE a.uid IN :uuids")
            .setParameter("uuids", uuids)
            .executeUpdate();
    }

    @Override
    public List<AuditEvent> find() {
        TypedQuery<AuditEventEntity> query = entityManager.createQuery(
            "SELECT a FROM AuditEventEntity a ORDER BY a.timestamp DESC",
            AuditEventEntity.class
        );
        return query.getResultList().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    private AuditEventEntity toEntity(AuditEvent domain) {
        AuditEventEntity entity = new AuditEventEntity();
        if (domain.getId() != null) {
            entity.setUid(UUID.fromString(domain.getId()));
        }
        entity.setTenantId(domain.getTenantId());
        entity.setUserId(domain.getUserId());
        entity.setTimestamp(domain.getTimestamp());
        entity.setEventType(domain.getEventType());
        entity.setEntityId(domain.getEntityId());
        entity.setEntityType(domain.getEntityType());
        entity.setPayload(domain.getPayload());
        entity.setProcessed(domain.isProcessed());
        return entity;
    }

    private AuditEvent toDomain(AuditEventEntity entity) {
        return AuditEvent.builder()
            .id(entity.getUid().toString())
            .tenantId(entity.getTenantId())
            .userId(entity.getUserId())
            .timestamp(entity.getTimestamp())
            .eventType(entity.getEventType())
            .entityId(entity.getEntityId())
            .entityType(entity.getEntityType())
            .payload(entity.getPayload())
            .processed(entity.isProcessed())
            .build();
    }
}
