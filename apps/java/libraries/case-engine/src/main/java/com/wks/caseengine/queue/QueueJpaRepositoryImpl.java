package com.wks.caseengine.queue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.wks.caseengine.entity.QueueEntity;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Profile("jpa")
public class QueueJpaRepositoryImpl implements QueueRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Queue> find() {
		return entityManager.createQuery("SELECT q FROM QueueEntity q", QueueEntity.class).getResultList().stream()
				.map(this::toDomain).collect(Collectors.toList());
	}

	@Override
	public Queue get(String id) throws DatabaseRecordNotFoundException {
		Optional<QueueEntity> queueEntity = findById(id);

		return queueEntity.map(this::toDomain)
				.orElseThrow(() -> new DatabaseRecordNotFoundException("Queue", "id", id));
	}

	@Override
	public String save(Queue queue) {
		QueueEntity entity = toEntity(queue);
		entityManager.persist(entity);
		return entity.getId();
	}

	@Override
	public void update(String id, Queue queue) throws DatabaseRecordNotFoundException {
		Optional<QueueEntity> from = findById(id);
		QueueEntity entity = from.get();
		entity.setName(queue.getName());
		entity.setDescription(queue.getDescription());
		entityManager.merge(entity);
	}

	@Override
	public void delete(String id) throws DatabaseRecordNotFoundException {
		Optional<QueueEntity> from = findById(id);
		QueueEntity entity = from.get();
		entityManager.remove(entity);
	}

	private Queue toDomain(QueueEntity entity) {
		Queue queue = new Queue();
		queue.setId(entity.getId());
		queue.setName(entity.getName());
		queue.setDescription(entity.getDescription());
		return queue;
	}

	private QueueEntity toEntity(Queue queue) {
		QueueEntity entity = new QueueEntity();
		entity.setId(queue.getId());
		entity.setName(queue.getName());
		entity.setDescription(queue.getDescription());
		return entity;
	}
	
	private Optional<QueueEntity> findById(String id) throws DatabaseRecordNotFoundException {
		Optional<QueueEntity> queueEntity =  Optional.ofNullable(entityManager
				.createQuery("SELECT f FROM QueueEntity f WHERE f.id = :id", QueueEntity.class)
				.setParameter("id", id).getResultStream().findFirst()
				.orElseThrow(() -> new DatabaseRecordNotFoundException("Queue", "id", id)));
		return queueEntity;
	}

}
