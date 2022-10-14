package com.wks.caseengine.repository;

import com.wks.caseengine.cases.definition.CaseDefinition;

public interface CaseDefinitionRepository extends Repository<CaseDefinition> {

	void updateCaseDefinition(final String caseDefId, final CaseDefinition caseDefinition) throws Exception;

}
