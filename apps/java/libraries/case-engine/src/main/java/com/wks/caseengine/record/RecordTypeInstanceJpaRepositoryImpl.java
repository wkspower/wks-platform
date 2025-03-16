/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.record;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;
import com.wks.caseengine.entity.RecordTypeInstanceEntity;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class RecordTypeInstanceJpaRepositoryImpl implements RecordRepository {

	 @PersistenceContext
	    private EntityManager entityManager;

	    @Override
	    public JsonObject get(final String recordTypeId, final String id) throws DatabaseRecordNotFoundException {
	        Optional<RecordTypeInstanceEntity> record = Optional.ofNullable(entityManager.createQuery(
	                "SELECT r FROM RecordTypeInstanceEntity r WHERE r.uid = :uid AND r.recordType = :recordType", RecordTypeInstanceEntity.class)
	        		.setParameter("uid", UUID.fromString(id))
	                .setParameter("recordType", recordTypeId)
	                .getSingleResult());

	        return record.map(mapper())
	        					   .orElseThrow(() -> new DatabaseRecordNotFoundException("Record", "uid", id));
	    }

	    @Override
	    public void save(final String recordTypeId, final JsonObject record) {
	        RecordTypeInstanceEntity entity = new RecordTypeInstanceEntity();
	        entity.setRecordType(recordTypeId);
	        entity.setContent(record);
	        entityManager.persist(entity);
	    }

	    @Override
	    public List<JsonObject> find(final String recordTypeId) {
	        return entityManager.createQuery(
	                "SELECT r FROM RecordTypeInstanceEntity r WHERE r.recordType = :recordType", RecordTypeInstanceEntity.class)
	                .setParameter("recordType", recordTypeId)
	                .getResultList()
	                .stream()
	                .map(mapper())
	                .collect(Collectors.toList());
	    }

	    @Override
	    public void delete(final String recordTypeId, final String id) throws DatabaseRecordNotFoundException {
	        RecordTypeInstanceEntity entity = entityManager.createQuery(
	                "SELECT r FROM RecordTypeInstanceEntity r WHERE r.uid = :uid AND r.recordType = :recordType", RecordTypeInstanceEntity.class)
	        		.setParameter("uid", UUID.fromString(id))
	                .setParameter("recordType", recordTypeId)
	                .getSingleResult();

	        if (entity == null) {
	            throw new DatabaseRecordNotFoundException("Record", "uid", id);
	        }

	        entityManager.remove(entity);
	    }

	    @Override
	    public void update(final String recordTypeId, final String id, final JsonObject record) throws DatabaseRecordNotFoundException {
	        RecordTypeInstanceEntity entity = entityManager.createQuery(
	                "SELECT r FROM RecordTypeInstanceEntity r WHERE r.uid = :uid AND r.recordType = :recordType", RecordTypeInstanceEntity.class)
	                .setParameter("uid", UUID.fromString(id))
	                .setParameter("recordType", recordTypeId)
	                .getSingleResult();

	        if (entity == null) {
	            throw new DatabaseRecordNotFoundException("Record", "uid", id);
	        }

	        entity.setContent(record);
	        entityManager.merge(entity);
	    }
	    
		private Function<? super RecordTypeInstanceEntity, ? extends JsonObject> mapper() {
			return (item)-> {
				JsonObject json = item.getContent();
				json.addProperty("id", item.getUid().toString());
				return json;
			};
		}
}
