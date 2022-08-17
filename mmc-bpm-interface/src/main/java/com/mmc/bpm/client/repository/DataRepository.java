package com.mmc.bpm.client.repository;

import java.util.List;

import com.mmc.bpm.client.cases.instance.CaseInstance;

public interface DataRepository {

	public List<CaseInstance> findCaseInstances() throws Exception;

	public CaseInstance getCaseInstance(final String businessKey) throws Exception;

	public void saveCaseInstance(final CaseInstance caseInstance) throws Exception;

	public void updateCaseStatus(final String businessKey, final String newStatus) throws Exception;

	public void delete(final CaseInstance caseInstance) throws Exception;

}
