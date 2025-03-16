package com.wks.caseengine.record.type;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.RecordTypeEntity;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class RecordTypeJpaRepositoryImpl implements RecordTypeRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public RecordType get(String id) throws DatabaseRecordNotFoundException {
		RecordTypeEntity entity = findById(id);
		
		return toDomain(entity);
	}

	public String save(RecordType recordType) {
		RecordTypeEntity entity = new RecordTypeEntity();
		entity.setFields(recordType.getFields());
		entity.setId(recordType.getId());
        entityManager.persist(entity);
	    return entity.getId().toString();
	}

	public List<RecordType> find() {
		List<RecordTypeEntity> entities = entityManager
				.createQuery("SELECT r FROM RecordTypeEntity r", RecordTypeEntity.class)
				.getResultList();
		
		return entities.stream().map(this::toDomain).collect(Collectors.toList());
	}

	public void delete(String id) throws DatabaseRecordNotFoundException {
		RecordTypeEntity entity = findById(id);
		
		entityManager.remove(entity);
	}

	public void update(String id, RecordType recordType) throws DatabaseRecordNotFoundException {
		RecordTypeEntity entity = findById(id);
		entity.setFields(recordType.getFields());
		entityManager.merge(entity);
	}

	private RecordType toDomain(RecordTypeEntity entity) {
		if (entity == null)
			return null;
		RecordType recordType = new RecordType();
		recordType.setId(entity.getId().toString());
		recordType.setFields(entity.getFields());
		return recordType;
	}

	private RecordTypeEntity findById(String id) throws DatabaseRecordNotFoundException {
		try {
			return entityManager.createQuery(
	                "SELECT r FROM RecordTypeEntity r WHERE r.id = :id", RecordTypeEntity.class)
	                .setParameter("id", id)
	                .getSingleResult();
	    } catch (NoResultException e) {
	        throw new DatabaseRecordNotFoundException("RecordType", "id", id);
	    }
	}
	
}