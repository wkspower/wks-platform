package com.mmc.bpm.engine.repository;

import java.util.List;

import com.mmc.bpm.engine.cases.instance.CaseInstance;

public interface DataRepository {

	public void saveCaseInstance(final CaseInstance caseInstance);

	public List<CaseInstance> findCaseInstances();

}
