package com.wks.caseengine.audit;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    private String id;
    private String tenantId;
    private String userId;
    private String timestamp;
    private AuditEventType eventType;
    private String entityId;
    private String entityType;
    private String payload;
    private boolean processed;
}
