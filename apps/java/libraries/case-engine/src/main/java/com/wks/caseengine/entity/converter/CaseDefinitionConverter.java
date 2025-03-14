package com.wks.caseengine.entity.converter;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.entity.CaseDefinitionEntity;

public class CaseDefinitionConverter {

    public static CaseDefinition toDomain(CaseDefinitionEntity entity) {
        if (entity == null) return null;

        CaseDefinition caseDefinition = new CaseDefinition();
        caseDefinition.setId(entity.getId());
        caseDefinition.setName(entity.getName());
        caseDefinition.setFormKey(entity.getFormKey());
        caseDefinition.setStagesLifecycleProcessKey(entity.getStagesLifecycleProcessKey());
        caseDefinition.setDeployed(entity.getDeployed());
        caseDefinition.setStages(entity.getStages());
        caseDefinition.setCaseHooks(entity.getCaseHooks());
        caseDefinition.setKanbanConfig(entity.getKanbanConfig());

        return caseDefinition;
    }

    public static CaseDefinitionEntity toEntity(CaseDefinition caseDefinition) {
        if (caseDefinition == null) return null;

        CaseDefinitionEntity entity = new CaseDefinitionEntity();
        entity.setId(caseDefinition.getId());
        entity.setName(caseDefinition.getName());
        entity.setFormKey(caseDefinition.getFormKey());
        entity.setStagesLifecycleProcessKey(caseDefinition.getStagesLifecycleProcessKey());
        entity.setDeployed(caseDefinition.getDeployed());
        entity.setStages(caseDefinition.getStages());
        entity.setCaseHooks(caseDefinition.getCaseHooks());
        entity.setKanbanConfig(caseDefinition.getKanbanConfig());

        return entity;
    }
}
