package com.wks.caseengine.cases.definition.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.entity.CaseDefinitionEntity;
import com.wks.caseengine.entity.converter.CaseDefinitionConverter;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional
@Profile("jpa")
public class CaseDefinitionJpaRepositoryImpl implements CaseDefinitionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CaseDefinition> find() {
        List<CaseDefinitionEntity> entities = entityManager.createQuery("SELECT c FROM CaseDefinitionEntity c", CaseDefinitionEntity.class)
        																						.getResultList();
        
        return entities.stream().map(CaseDefinitionConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<CaseDefinition> find(Optional<Boolean> deployed) {
        if (deployed.isPresent()) {
            List<CaseDefinitionEntity> entities = entityManager
                    .createQuery("SELECT c FROM CaseDefinitionEntity c WHERE c.deployed = :deployed", CaseDefinitionEntity.class)
                    .setParameter("deployed", deployed.get())
                    .getResultList();
            return entities.stream().map(CaseDefinitionConverter::toDomain).collect(Collectors.toList());
        }
        
        return find();
    }

    @Override
    public CaseDefinition get(String caseDefId) throws DatabaseRecordNotFoundException {
        CaseDefinitionEntity entity = findById(caseDefId);
        return CaseDefinitionConverter.toDomain(entity);
    }
	
    @Override
    public String save(CaseDefinition caseDefinition) {
        CaseDefinitionEntity entity = CaseDefinitionConverter.toEntity(caseDefinition);
        entityManager.persist(entity);
        return entity.getId();
    }

    @Override
    public void update(String caseDefId, CaseDefinition caseDefinition) throws DatabaseRecordNotFoundException {
        CaseDefinitionEntity existingEntity = findById(caseDefId);

        existingEntity.setName(caseDefinition.getName());
        existingEntity.setFormKey(caseDefinition.getFormKey());
        existingEntity.setStagesLifecycleProcessKey(caseDefinition.getStagesLifecycleProcessKey());
        existingEntity.setDeployed(caseDefinition.getDeployed());
        existingEntity.setStages(caseDefinition.getStages());
        existingEntity.setCaseHooks(caseDefinition.getCaseHooks());
        existingEntity.setKanbanConfig(caseDefinition.getKanbanConfig());

        entityManager.merge(existingEntity);
    }

    @Override
    public void delete(String caseDefinitionId) throws DatabaseRecordNotFoundException {
    	CaseDefinitionEntity entity = findById(caseDefinitionId);
        entityManager.remove(entity);
    }
    
	private CaseDefinitionEntity findById(String id) throws DatabaseRecordNotFoundException {
		try {
			return entityManager.createQuery(
	                "SELECT r FROM CaseDefinitionEntity r WHERE r.id = :id", CaseDefinitionEntity.class)
	                .setParameter("id", id)
	                .getSingleResult();
	    } catch (NoResultException e) {
	        throw new DatabaseRecordNotFoundException("RecordType", "id", id);
	    }
	}
    
}
