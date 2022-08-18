package com.mmc.bpm.client.repository;

import java.util.List;

import com.mmc.bpm.client.cases.definition.CaseDefinition;
import com.mmc.bpm.client.cases.instance.CaseInstance;

public interface DataRepository {

	// Case Definition operations
	public List<CaseDefinition> findCaseDefintions() throws Exception;

	public CaseDefinition getCaseDefinition(final String caseDefId) throws Exception;

	public void saveCaseDefinition(final CaseDefinition caseDefinition) throws Exception;

	public void deleteCaseDefinition(final String caseDefinitionId) throws Exception;

	// Case operations
	public List<CaseInstance> findCaseInstances() throws Exception;

	public CaseInstance getCaseInstance(final String businessKey) throws Exception;

	public void saveCaseInstance(final CaseInstance caseInstance) throws Exception;

	public void updateCaseStatus(final String businessKey, final String newStatus) throws Exception;

	public void deleteCase(final CaseInstance caseInstance) throws Exception;

}
