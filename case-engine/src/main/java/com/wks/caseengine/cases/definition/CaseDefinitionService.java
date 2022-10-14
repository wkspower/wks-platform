package com.wks.caseengine.cases.definition;

import java.util.List;

import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;

public interface CaseDefinitionService {

	List<CaseDefinition> find() throws Exception;

	CaseDefinition get(final String caseDefId) throws Exception;

	CaseDefinition create(final CaseDefinition caseDefinition) throws Exception;
	
	CaseDefinition update(final String caseDefId, CaseDefinition caseDefinition) throws Exception;

	void delete(final String caseDefinitionId) throws CaseInstanceNotFoundException, Exception;

}
