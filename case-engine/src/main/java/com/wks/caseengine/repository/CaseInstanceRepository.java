package com.wks.caseengine.repository;

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;

public interface CaseInstanceRepository extends Repository<CaseInstance> {

	List<CaseInstance> findCaseInstances(final Optional<CaseStatus> status) throws Exception;

	void updateCaseStatus(final String businessKey, final CaseStatus newStatus) throws Exception;

	void updateCaseStage(final String businessKey, final String caseStage);
	
}
