package com.wks.caseengine.event;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WksEvent {
    String id;
    String tenantId;
    String userId;
    String eventType;
    String entityId;
    String entityType;
    String payload;
    String timestamp;
}
