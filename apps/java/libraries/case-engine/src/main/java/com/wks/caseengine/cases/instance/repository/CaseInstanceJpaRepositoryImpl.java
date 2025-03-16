package com.wks.caseengine.cases.instance.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.entity.CaseInstanceEntity;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;
import com.wks.caseengine.repository.JpaPaginator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class CaseInstanceJpaRepositoryImpl implements CaseInstanceRepository {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
    private JpaPaginator paginator;

	@Override
	public List<CaseInstance> find() {
		 TypedQuery<CaseInstanceEntity> query = entityManager.createQuery("SELECT c FROM CaseInstanceEntity c ORDER BY c.id DESC", CaseInstanceEntity.class);
		 query = paginator.apply(query);
		 return query.getResultList().stream()
	                .map(this::toDomain)
	                .collect(Collectors.toList());
	}

	@Override
	public PageResult<CaseInstance> find(CaseInstanceFilter filters) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	    CriteriaQuery<CaseInstanceEntity> cq = cb.createQuery(CaseInstanceEntity.class);
	    Root<CaseInstanceEntity> root = cq.from(CaseInstanceEntity.class);

	    List<Predicate> predicates = new ArrayList<>();

	    filters.getCaseDefsId().ifPresent(businessKey -> 
	        predicates.add(cb.equal(root.get("businessKey"), businessKey))
	    );

	    filters.getStatus().ifPresent(status -> 
	        predicates.add(cb.equal(root.get("status"), status))
	    );

	    Cursor cursor = filters.getCursor();
	    if (cursor != null) {
	        if (cursor.hasPrevious()) {
	            predicates.add(cb.greaterThan(root.get("uid"), UUID.fromString(cursor.previous())));
	        } else if (cursor.hasNext()) {
	            predicates.add(cb.lessThan(root.get("uid"), UUID.fromString(cursor.next())));
	        }
	    }

	    if (cursor != null && cursor.hasPrevious()) {
	        cq.orderBy(cb.asc(root.get("uid")));
	    } else {
	        cq.orderBy(cb.desc(root.get("uid")));
	    }

	    TypedQuery<CaseInstanceEntity> query = entityManager.createQuery(cq);
	    query.setMaxResults(filters.getLimit());

	    List<CaseInstance> results = query.getResultList().stream()
	            .map(this::toDomain)
	            .collect(Collectors.toList());

	    String nextCursor = results.isEmpty() ? null : results.get(results.size() - 1).getId();
	    String previousCursor = results.isEmpty() ? null : results.get(0).getId();

	    return new PageResult<>(results, true, false, nextCursor, previousCursor, Direction.ASC, filters.getLimit());
	}

	@Override
	public CaseInstance get(final String businessKey) throws DatabaseRecordNotFoundException {
		Optional<CaseInstanceEntity> entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey", CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst();

		return entity.map(this::toDomain)
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));
	}

	@Override
	public String save(final CaseInstance caseInstance) {
		CaseInstanceEntity entity = toEntity(caseInstance);
		entityManager.persist(entity);
		return entity.getUid().toString();
	}

	@Override
	public void update(final String businessKey, final CaseInstance caseInstance) throws DatabaseRecordNotFoundException {
		CaseInstanceEntity entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey", CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));

		CaseStatus status = caseInstance.getStatus();
		if (status != null) {
			entity.setStatus(status.getCode());
		}
		
		entity.setStage(caseInstance.getStage());
		entity.setAttributes(caseInstance.getAttributes());
		entity.setDocuments(caseInstance.getDocuments());
		entity.setQueueId(caseInstance.getQueueId());
		entity.setComments(caseInstance.getComments());

		entityManager.merge(entity);
	}

	@Override
	public void delete(final String businessKey) throws DatabaseRecordNotFoundException {
		CaseInstanceEntity entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey",
						CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));

		entityManager.remove(entity);
	}

	@Override
	public void deleteComment(final String businessKey, final CaseComment comment)
			throws DatabaseRecordNotFoundException {

		CaseInstanceEntity entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey",
						CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));

		entity.getComments().removeIf(c -> c.getId().equals(comment.getId()));
		entityManager.merge(entity);
	}

	@Override
	public void updateComment(final String businessKey, final String commentId, final String body)
			throws DatabaseRecordNotFoundException {
		CaseInstanceEntity entity = entityManager
				.createQuery("SELECT c FROM CaseInstanceEntity c WHERE c.businessKey = :businessKey",
						CaseInstanceEntity.class)
				.setParameter("businessKey", businessKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseInstance", "businessKey", businessKey));

		entity.getComments().forEach(comment -> {
			if (comment.getId().equals(commentId)) {
				comment.setBody(body);
			}
		});

		entityManager.merge(entity);
	}

	private CaseInstance toDomain(CaseInstanceEntity entity) {
		CaseInstance caseInstance = new CaseInstance();
		caseInstance.set_id(entity.getUid().toString());
		caseInstance.setBusinessKey(entity.getBusinessKey());
		caseInstance.setStatus(CaseStatus.fromValue(entity.getStatus()).orElse(null));
		caseInstance.setStage(entity.getStage());
		caseInstance.setAttributes(entity.getAttributes());
		caseInstance.setDocuments(entity.getDocuments());
		caseInstance.setQueueId(entity.getQueueId());
		caseInstance.setComments(entity.getComments());
		caseInstance.setOwner(entity.getOwner());
		caseInstance.setCaseDefinitionId(entity.getCaseDefinitionId());
		return caseInstance;
	}

	private CaseInstanceEntity toEntity(CaseInstance caseInstance) {
		CaseInstanceEntity entity = new CaseInstanceEntity();
		entity.setBusinessKey(caseInstance.getBusinessKey());
		
		if (caseInstance.getStatus() != null) {
			entity.setStatus(caseInstance.getStatus().getCode());
		}
		
		entity.setStage(caseInstance.getStage());
		entity.setAttributes(caseInstance.getAttributes());
		entity.setDocuments(caseInstance.getDocuments());
		entity.setQueueId(caseInstance.getQueueId());
		entity.setComments(caseInstance.getComments());
		entity.setOwner(caseInstance.getOwner());
		entity.setCaseDefinitionId(caseInstance.getCaseDefinitionId());
		
		return entity;
	}

}
