package com.wks.caseengine.audit.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.GsonBuilder;
import com.wks.caseengine.audit.AuditEvent;
import com.wks.caseengine.audit.repository.AuditEventRepository;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("audit")
@Tag(name = "Audit Trail & Case Feed", description = "Endpoints for compliance audit trail and case timeline events")
@ConditionalOnProperty(name = "wks.audit.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AuditEventController {

    private final AuditEventRepository auditEventRepository;
    private final GsonBuilder gsonBuilder;

    @GetMapping(value = "/trail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAuditTrail() {
        List<AuditEvent> trail = auditEventRepository.find();
        return ResponseEntity.ok(gsonBuilder.create().toJson(trail));
    }

    @GetMapping(value = "/case/{businessKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCaseFeed(@PathVariable("businessKey") String businessKey) {
        List<AuditEvent> feed = auditEventRepository.findByCaseInstanceId(businessKey);
        return ResponseEntity.ok(gsonBuilder.create().toJson(feed));
    }
}
