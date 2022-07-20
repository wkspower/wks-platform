package com.mmc.bpm.repository;

import java.util.List;

import com.mmc.bpm.cases.instance.CaseInstance;

public interface DataRepository {

	public List<CaseInstance> findCaseInstances() throws Exception;

	public void saveCaseInstance(final CaseInstance caseInstance) throws Exception;

	public void delete(final CaseInstance caseInstance) throws Exception;

}
