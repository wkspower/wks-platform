package com.wks.caseengine.form;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.jpa.entity.FormEntity;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class FormJpaRepositoryImpl implements FormRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Form get(final String formKey) throws DatabaseRecordNotFoundException {
		Optional<FormEntity> formEntity = entityManager
				.createQuery("SELECT f FROM FormEntity f WHERE f.key = :formKey", FormEntity.class)
				.setParameter("formKey", formKey).getResultStream().findFirst();

		return formEntity.map(this::toDomain)
				.orElseThrow(() -> new DatabaseRecordNotFoundException("Form", "key", formKey));
	}

	@Override
	public String save(final Form form) {
		FormEntity entity = toEntity(form);
		entityManager.persist(entity);
		return entity.getUid().toString();
	}

	@Override
	public List<Form> find() {
		return entityManager.createQuery("SELECT f FROM FormEntity f", FormEntity.class).getResultList().stream()
				.map(this::toDomain).collect(Collectors.toList());
	}

	@Override
	public void delete(final String formKey) throws DatabaseRecordNotFoundException {
		FormEntity entity = entityManager
				.createQuery("SELECT f FROM FormEntity f WHERE f.key = :formKey", FormEntity.class)
				.setParameter("formKey", formKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("Form", "key", formKey));

		entityManager.remove(entity);
	}

	@Override
	public void update(final String formKey, final Form form) throws DatabaseRecordNotFoundException {
		FormEntity entity = entityManager
				.createQuery("SELECT f FROM FormEntity f WHERE f.key = :formKey", FormEntity.class)
				.setParameter("formKey", formKey).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("Form", "key", formKey));

		entity.setTitle(form.getTitle());
		entity.setToolTip(form.getToolTip());
		entity.setStructure(form.getStructure());

		entityManager.merge(entity);
	}

	private Form toDomain(FormEntity entity) {
		Form form = new Form();
		form.setKey(entity.getKey());
		form.setTitle(entity.getTitle());
		form.setToolTip(entity.getToolTip());
		form.setStructure(entity.getStructure());
		return form;
	}

	private FormEntity toEntity(Form form) {
		FormEntity entity = new FormEntity();
		entity.setKey(form.getKey());
		entity.setTitle(form.getTitle());
		entity.setToolTip(form.getToolTip());
		entity.setStructure(form.getStructure());
		return entity;
	}

}
