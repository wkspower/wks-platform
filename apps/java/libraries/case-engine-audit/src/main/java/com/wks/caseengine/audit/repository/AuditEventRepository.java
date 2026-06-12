package com.wks.caseengine.audit.repository;

import java.util.List;
import com.wks.caseengine.audit.AuditEvent;

public interface AuditEventRepository {
    void save(AuditEvent auditEvent);
    List<AuditEvent> findByCaseInstanceId(String caseInstanceId);
    List<AuditEvent> findUnprocessed();
    void markProcessed(List<String> ids);
    List<AuditEvent> find();
}
