package com.mmc.bpm.client.repository;

import java.util.List;

import com.mmc.bpm.client.cases.instance.CaseInstance;

public interface DataRepository {

	public void saveCaseInstance(final CaseInstance caseInstance);

	public List<CaseInstance> findCaseInstances();

}
