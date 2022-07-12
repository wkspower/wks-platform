package com.mmc.automation.platform.casemanagement.repository;

import java.util.List;

import com.mmc.automation.platform.casemanagement.cases.instance.CaseInstance;

public interface DataRepository {

	public void saveCaseInstance(final CaseInstance caseInstance);

	public List<CaseInstance> findCaseInstances();

}
