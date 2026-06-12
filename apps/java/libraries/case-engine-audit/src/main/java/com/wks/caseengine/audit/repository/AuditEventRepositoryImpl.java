package com.wks.caseengine.audit.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.wks.caseengine.audit.AuditEvent;
import com.wks.caseengine.db.EngineMongoDataConnection;

@Primary
@Component
@ConditionalOnProperty(name = "database.type", havingValue = "mongo", matchIfMissing = false)
public class AuditEventRepositoryImpl implements AuditEventRepository {

    private static final String COLLECTION_NAME = "auditEvents";

    @Autowired
    private EngineMongoDataConnection connection;

    @Override
    public void save(AuditEvent auditEvent) {
        if (auditEvent.getId() == null) {
            auditEvent.setId(new ObjectId().toHexString());
        }
        getOperations().save(auditEvent, COLLECTION_NAME);
    }

    @Override
    public List<AuditEvent> findByCaseInstanceId(String caseInstanceId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("entityId").is(caseInstanceId)
                .and("entityType").is("CaseInstance"));
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return getOperations().find(query, AuditEvent.class, COLLECTION_NAME);
    }

    @Override
    public List<AuditEvent> findUnprocessed() {
        Query query = new Query();
        query.addCriteria(Criteria.where("processed").is(false));
        query.with(Sort.by(Sort.Direction.ASC, "timestamp"));
        return getOperations().find(query, AuditEvent.class, COLLECTION_NAME);
    }

    @Override
    public void markProcessed(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("id").in(ids));
        Update update = new Update();
        update.set("processed", true);
        getOperations().updateMulti(query, update, AuditEvent.class, COLLECTION_NAME);
    }

    @Override
    public List<AuditEvent> find() {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return getOperations().find(query, AuditEvent.class, COLLECTION_NAME);
    }

    private MongoOperations getOperations() {
        return connection.getOperations();
    }
}
