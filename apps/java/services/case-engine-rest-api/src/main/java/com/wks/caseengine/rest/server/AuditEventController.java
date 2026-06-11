package com.wks.caseengine.rest.server;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("audit")
@Tag(name = "Audit Trail & Case Feed", description = "Endpoints for compliance audit trail and case timeline events")
public class AuditEventController {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private GsonBuilder gsonBuilder;

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
