package com.mmc.bpm.client.cases.definition;

import java.util.List;

import com.mmc.bpm.client.cases.instance.CaseInstanceNotFoundException;

public interface CaseDefinitionService {

	public List<CaseDefinition> find() throws Exception;

	public CaseDefinition get(final String caseDefId) throws Exception;

	public CaseDefinition create(final CaseDefinition caseDefinition) throws Exception;

	public void delete(final String caseDefinitionId) throws CaseInstanceNotFoundException, Exception;
}
