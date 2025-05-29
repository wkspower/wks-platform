package com.wks.caseengine.cases.instance.email.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.instance.email.CaseEmail;
import com.wks.caseengine.jpa.entity.CaseEmailEntity;
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
public class CaseEmailJpaRepositoryImpl implements CaseEmailRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private JpaPaginator paginator;

	@Override
	public List<CaseEmail> find() {
		TypedQuery<CaseEmailEntity> query = entityManager
				.createQuery("SELECT e FROM CaseEmailEntity e ORDER BY e.uid DESC", CaseEmailEntity.class);
		query = paginator.apply(query);

		return query.getResultList().stream().map(this::toDomain).collect(Collectors.toList());
	}

	@Override
	public List<CaseEmail> find(final Optional<String> caseInstanceBusinessKey) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CaseEmailEntity> cq = cb.createQuery(CaseEmailEntity.class);
		Root<CaseEmailEntity> root = cq.from(CaseEmailEntity.class);

		List<Predicate> predicates = new ArrayList<>();

		caseInstanceBusinessKey.ifPresent(key -> predicates.add(cb.equal(root.get("caseInstanceBusinessKey"), key)));

		cq.where(predicates.toArray(new Predicate[0]));
		cq.orderBy(cb.desc(root.get("uid"))); // Ordenação decrescente pelo UUID

		TypedQuery<CaseEmailEntity> query = entityManager.createQuery(cq);
		query = paginator.apply(query); // Aplica paginação

		return query.getResultList().stream().map(this::toDomain).collect(Collectors.toList());
	}

	@Override
	public CaseEmail get(String caseEmailId) throws DatabaseRecordNotFoundException {
		UUID emailUid = UUID.fromString(caseEmailId);
		Optional<CaseEmailEntity> entity = Optional.ofNullable(entityManager.find(CaseEmailEntity.class, emailUid));

		return entity.map(this::toDomain)
				.orElseThrow(() -> new DatabaseRecordNotFoundException("CaseEmail", "uid", caseEmailId));
	}

	@Override
	public String save(final CaseEmail caseEmail) {
		CaseEmailEntity entity = toEntity(caseEmail);
		entityManager.persist(entity);
		return entity.getUid().toString();
	}

	@Override
	public void update(String id, CaseEmail caseEmail) {
		UUID emailUid = UUID.fromString(id);
		CaseEmailEntity entity = entityManager.find(CaseEmailEntity.class, emailUid);

		entity.setCaseInstanceBusinessKey(caseEmail.getCaseInstanceBusinessKey());
		entity.setSubject(caseEmail.getSubject());
		entity.setBody(caseEmail.getBody());
		entity.setReceivedDateTime(caseEmail.getReceivedDateTime());
		entity.setHasAttachments(caseEmail.getHasAttachments());
		entity.setTo(caseEmail.getTo());
		entity.setFrom(caseEmail.getFrom());
		entity.setBodyPreview(caseEmail.getBodyPreview());
		entity.setImportance(caseEmail.getImportance());
		entity.setCaseDefinitionId(caseEmail.getCaseDefinitionId());
		entity.setOutbound(caseEmail.isOutbound());
		entity.setStatus(caseEmail.getStatus());

		entityManager.merge(entity);
	}

	@Override
	public void delete(String id) {
		throw new UnsupportedOperationException();
	}

	private CaseEmail toDomain(CaseEmailEntity entity) {
		CaseEmail caseEmail = new CaseEmail();
		caseEmail.set_id(entity.getUid().toString());
		caseEmail.setCaseInstanceBusinessKey(entity.getCaseInstanceBusinessKey());
		caseEmail.setSubject(entity.getSubject());
		caseEmail.setBody(entity.getBody());
		caseEmail.setReceivedDateTime(entity.getReceivedDateTime());
		caseEmail.setHasAttachments(entity.getHasAttachments());
		caseEmail.setTo(entity.getTo());
		caseEmail.setFrom(entity.getFrom());
		caseEmail.setBodyPreview(entity.getBodyPreview());
		caseEmail.setImportance(entity.getImportance());
		caseEmail.setCaseDefinitionId(entity.getCaseDefinitionId());
		caseEmail.setOutbound(entity.getOutbound());
		caseEmail.setStatus(entity.getStatus());
		return caseEmail;
	}

	private CaseEmailEntity toEntity(CaseEmail caseEmail) {
		CaseEmailEntity entity = new CaseEmailEntity();
		entity.setUid(caseEmail.get_id() != null ? UUID.fromString(caseEmail.get_id()) : null);
		entity.setCaseInstanceBusinessKey(caseEmail.getCaseInstanceBusinessKey());
		entity.setSubject(caseEmail.getSubject());
		entity.setBody(caseEmail.getBody());
		entity.setReceivedDateTime(caseEmail.getReceivedDateTime());
		entity.setHasAttachments(caseEmail.getHasAttachments());
		entity.setTo(caseEmail.getTo());
		entity.setFrom(caseEmail.getFrom());
		entity.setBodyPreview(caseEmail.getBodyPreview());
		entity.setImportance(caseEmail.getImportance());
		entity.setCaseDefinitionId(caseEmail.getCaseDefinitionId());
		entity.setOutbound(caseEmail.isOutbound());
		entity.setStatus(caseEmail.getStatus());
		return entity;
	}

}
