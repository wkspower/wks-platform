package com.wks.caseengine.jpa.entity;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import com.wks.caseengine.audit.AuditEventType;

@Entity
@Table(name = "wks_audit_event")
@Getter
@Setter
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uid;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private AuditEventType eventType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "processed")
    private boolean processed;
}
